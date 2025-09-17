//package cn.piesat.nj.slardar.example.impl;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
///**
// * <p>
// * .
// * </p>
// *
// * @author Alex
// * @version v1.0 2024/1/2
// */
//@Component
//public class MyPasswordEncoder implements PasswordEncoder {
//    @Override
//    public String encode(CharSequence rawPassword) {
//        return rawPassword.toString();
//    }
//
//    @Override
//    public boolean matches(CharSequence rawPassword, String encodedPassword) {
//        System.out.println("------my encoder--------");
//        return rawPassword.toString().equalsIgnoreCase(encodedPassword);
//    }
//}
//
