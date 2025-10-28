/*
 * @Author: yxfacw@163.com
 * @Date: 2025-09-23 14:06:29
 * @LastEditTime: 2025-09-25 16:41:26
 * @LastEditors: yxfacw@163.com
 * @Description: .
 */
import {LitElement, html, css} from "../lib/lit-core.min.js";

class LicStatus extends LitElement {
    static properties = {
        data: {},
        productCode: {state: true},
    };

    constructor() {
        super();
        this.productCode = "";
        fetch("./license/status")
            .then((res) => res.json())
            .then((data) => {
                console.log(data);
                const {data: licData} = data
                this.data = licData;
                this.productCode = licData.productCode;
            });
    }

    /**
     * 不使用 shadow dom
     * @returns
     */
    createRenderRoot() {
        return this;
    }

    doRefresh() {
        // 发布自定义事件
        this.dispatchEvent(
            new CustomEvent("license-refresh", {
                detail: {data: this.data, productCode: this.productCode},
                bubbles: true,
                composed: true,
            })
        );
    }

    _codeChanged(e) {
        const val = e.target.value;
        if (val) {
            this.productCode = val;
        }
    }

    render() {
        return html`
            <form class="pure-form pure-form-aligned">
                <fieldset>
                    <div class="pure-control-group">
                        <label for="lic-status" data-i18n="status">许可状态</label>
                        <input
                                id="lic-status"
                                type="text"
                                disabled
                                value="${this.data.status}"
                        />
                    </div>
                    <div class="pure-control-group">
                        <label for="aligned-email" data-i18n="expired">许可有效期</label>
                        <input
                                id="aligned-email"
                                type="text"
                                disabled
                                value="${this.data.expiryDate}"
                        />
                    </div>
                    <div class="pure-control-group">
                        <label for="product" data-i18n="product">授权产品码</label>
                        <input
                                id="product"
                                type="text"
                                data-i18n-placeholder="product"
                                ${this.productCode ? "disabled" : ""}
                                .value=${this.productCode}
                                @change=${this._codeChanged}
                        />
                    </div>
                    <div class="pure-control-group">
                        <label for="lic-status" data-i18n="licFile">许可文件位置</label>
                        <textarea
                                id="lic-status"
                                disabled
                                rows="2">${this.data.licFile}</textarea>
                    </div>
                </fieldset>
            </form>
            <div class="flex-row items-center jusitify-center w-full">
                <button type="submit" class="button-success pure-button" @click="${
                        this.doRefresh
                }">
                    <i class="fa-solid fa-rotate"></i>
                    <span data-i18n="refresh">刷新许可</span>
                </button>
            </div
        `;
    }
}

customElements.define("lic-status", LicStatus);
