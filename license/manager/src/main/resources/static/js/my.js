/*
 * @Author: yxfacw@163.com
 * @Date: 2025-09-23 12:03:49
 * @LastEditTime: 2025-09-23 17:16:28
 * @LastEditors: yxfacw@163.com
 * @Description: .
 */

import "./components/language-toggle.js";
import "./components/lic-status.js";

// Translation dictionary
const translations = {
  en: {
    title: "License Manager",
    status: "License Status",
    licFile: "License File Path",
    product: "Product Code",
    expired: "License Expired Date",
    refresh: "Refresh License",
    confirmText:
      "Are you sure you want to refresh the license? <b>Refreshing will re-read the license</b>",
    submitText: "Yes",
    cancelText: "Cancel",
  },
  zh: {
    title: "许可证管理器",
    status: "许可状态",
    licFile: "许可文件位置",
    product: "授权产品码",
    expired: "许可失效日期",
    refresh: "刷新许可",
    confirmText: "确定要刷新许可证吗？<b>刷新后会重新读取许可</b>",
    submitText: "确定",
    cancelText: "取消",
  },
};

let lang = navigator.language.startsWith("zh") ? "zh" : "en";

// Function to update page content based on language
function updateLanguage() {
  // Update elements with data-i18n attributes
  document.querySelectorAll("[data-i18n]").forEach((element) => {
    const key = element.getAttribute("data-i18n");
    if (translations[lang] && translations[lang][key]) {
      element.textContent = translations[lang][key];
    }
  });

  // Update placeholders
  document.querySelectorAll("[data-i18n-placeholder]").forEach((element) => {
    const key = element.getAttribute("data-i18n-placeholder");
    if (translations[lang] && translations[lang][key]) {
      element.placeholder = translations[lang][key];
    }
  });

  // Update title
  if (translations[lang] && translations[lang]["title"]) {
    document.title = translations[lang]["title"];
  }
}

// Initialize with default language
document.addEventListener("DOMContentLoaded", () => {
  updateLanguage();
});

// Example of handling language change
document.addEventListener("language-changed", (e) => {
  lang = e.detail.language;
  updateLanguage();
});

document.addEventListener("license-refresh", (e) => {
  console.log("License refresh to:", e.detail);
  notie.confirm({
    text: translations[lang]["confirmText"],
    submitText: translations[lang]["submitText"],
    cancelText: translations[lang]["cancelText"],
    cancelCallback: function () {
    },
    submitCallback: function () {
     // TODO: 刷新
     fetch(`./license/install?product=${e.detail.productCode}`, {
        method: "POST"
      })
        .then((response) => response.json())
        .then((data) => {
          console.log("License refresh response:", data);
          notie.alert({
            type: 1,
          })
        })
    },
  });
});
