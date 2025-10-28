/*
 * @Author: yxfacw@163.com
 * @Date: 2025-09-23 13:38:05
 * @LastEditTime: 2025-09-23 13:52:26
 * @LastEditors: yxfacw@163.com
 * @Description: .
 */
// js/components/language-toggle.js
import { LitElement, html, css } from "../lib/lit-core.min.js";

class LanguageToggle extends LitElement {
  static properties = {
    currentLanguage: { type: String },
  };

  constructor() {
    super();
    this.currentLanguage = "zh"; // Default language
  }

  toggleLanguage() {
    this.currentLanguage = this.currentLanguage === "en" ? "zh" : "en";
    // 发布自定义事件
    this.dispatchEvent(
      new CustomEvent("language-changed", {
        detail: { language: this.currentLanguage },
        bubbles: true,
        composed: true,
      })
    );
  }

  /**
   * 不使用 shadow dom
   * @returns 
   */
  createRenderRoot() {
    return this;
  }

  render() {
    const buttonText = this.currentLanguage === "zh" ? "中文" : "EN";

    return html`
      <button
        class="pure-button button-xs button-secondary"
        @click="${this.toggleLanguage}"
      >
        <i class="fa-solid fa-language"></i>&nbsp;${buttonText}
      </button>
    `;
  }
}

customElements.define("language-toggle", LanguageToggle);
