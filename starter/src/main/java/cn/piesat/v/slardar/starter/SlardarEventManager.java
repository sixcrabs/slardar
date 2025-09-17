package cn.piesat.v.slardar.starter;

import cn.piesat.v.slardar.core.event.SlardarEvent;
import cn.piesat.v.slardar.core.event.SlardarEventListener;
import cn.piesat.v.slardar.core.SlardarException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 收集事件监听处理
 * 派发事件 异步线程触发
 * </p>
 *
 * @author alex
 * @version v1.0 2023/3/31
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class SlardarEventManager implements ApplicationContextAware {

    private ApplicationContext context;

    private static final Map<Class<? extends SlardarEvent>, List<SlardarEventListener>> CACHE = new ConcurrentHashMap<>(
            1);

    private static final ThreadPoolExecutor POOL = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2, 30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1024), new ThreadFactoryBuilder()
                    .setDaemon(true).setNameFormat("slardar-event-thread-%d").build(),
            new ThreadPoolExecutor.AbortPolicy());

    public <T extends SlardarEvent> void dispatch(T event) throws SlardarException {
        List<SlardarEventListener> candidates = findCandidates(event.getClass());
        // 提交到线程池中运行
        candidates.forEach(slardarEventListener -> {
            POOL.execute(() -> {
                try {
                    slardarEventListener.onEvent(event);
                } catch (SlardarException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private List<SlardarEventListener> findCandidates(Class<? extends SlardarEvent> eventClass) {
        return CACHE.computeIfAbsent(eventClass, clazz -> {
            Map<String, SlardarEventListener> listenerBeans = context.getBeansOfType(SlardarEventListener.class, true,
                    true);
            return listenerBeans.values().stream()
                    .filter(slardarEventListener -> slardarEventListener.support(eventClass))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @PreDestroy
    public void destroy() {
        POOL.shutdown();
    }
}
