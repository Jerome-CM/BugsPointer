(function () {
    "use strict";

    const script = document.currentScript;
    const baseUrl = new URL(script.src).origin;
    const publicKey = script.dataset.publicKey;

    if (!publicKey) {
        console.warn("Bugspointer: data-public-key is required.");
        return;
    }

    const defaults = {
        primaryColor: "#27215F",
        modalBackgroundColor: "#FFFFFF",
        modalTextColor: "#24233D",
        titleColor: "#24233D",
        linkTextColor: "#27215F",
        linkUnderline: true,
        buttonText: "Signaler un bug",
        buttonStyle: "button",
        buttonSize: 56,
        title: "Signaler un nouveau bug",
        descriptionLabel: "Description du bug",
        collectEmail: false,
        position: "bottom-right",
        marginX: 15,
        marginY: 15
    };

    const state = {
        config: { ...defaults },
        pointing: false,
        pointed: false,
        selectedBorder: "",
        selectedElement: null
    };

    const host = document.createElement("div");
    host.id = "bugspointer-widget-root";
    document.body.appendChild(host);
    const shadow = host.attachShadow({ mode: "open" });

    loadConfig().then((config) => {
        state.config = normalizeConfig({ ...defaults, ...config });
        render();
        bindEvents();
    });

    async function loadConfig() {
        const localConfig = readDatasetConfig();
        try {
            const response = await fetch(`${baseUrl}/api/widget/config?public_key=${encodeURIComponent(publicKey)}`, {
                method: "GET",
                mode: "cors",
                credentials: "omit"
            });
            if (!response.ok) {
                return localConfig;
            }
            return { ...(await response.json()), ...localConfig };
        } catch (error) {
            console.warn("Bugspointer: unable to load widget config.", error);
            return localConfig;
        }
    }

    function readDatasetConfig() {
        const config = {};
        if (script.dataset.color) config.primaryColor = script.dataset.color;
        if (script.dataset.modalBackgroundColor) config.modalBackgroundColor = script.dataset.modalBackgroundColor;
        if (script.dataset.modalTextColor) config.modalTextColor = script.dataset.modalTextColor;
        if (script.dataset.titleColor) config.titleColor = script.dataset.titleColor;
        if (script.dataset.linkTextColor) config.linkTextColor = script.dataset.linkTextColor;
        if (script.dataset.linkUnderline) config.linkUnderline = script.dataset.linkUnderline;
        if (script.dataset.buttonText) config.buttonText = script.dataset.buttonText;
        if (script.dataset.buttonStyle) config.buttonStyle = script.dataset.buttonStyle;
        if (script.dataset.buttonSize) config.buttonSize = script.dataset.buttonSize;
        if (script.dataset.title) config.title = script.dataset.title;
        if (script.dataset.descriptionLabel) config.descriptionLabel = script.dataset.descriptionLabel;
        if (script.dataset.collectEmail) config.collectEmail = script.dataset.collectEmail;
        if (script.dataset.position) config.position = script.dataset.position;
        if (script.dataset.marginX) config.marginX = script.dataset.marginX;
        if (script.dataset.marginY) config.marginY = script.dataset.marginY;
        return config;
    }

    function normalizeConfig(config) {
        return {
            primaryColor: safeColor(config.primaryColor),
            modalBackgroundColor: safeColor(config.modalBackgroundColor, defaults.modalBackgroundColor),
            modalTextColor: safeColor(config.modalTextColor, defaults.modalTextColor),
            titleColor: safeColor(config.titleColor, defaults.titleColor),
            linkTextColor: safeColor(config.linkTextColor, defaults.linkTextColor),
            linkUnderline: config.linkUnderline === true || config.linkUnderline === "true",
            buttonText: config.buttonText || defaults.buttonText,
            buttonStyle: safeButtonStyle(config.buttonStyle),
            buttonSize: safeButtonSize(config.buttonSize),
            title: config.title || defaults.title,
            descriptionLabel: config.descriptionLabel || defaults.descriptionLabel,
            collectEmail: config.collectEmail === true || config.collectEmail === "true",
            position: safePosition(config.position),
            marginX: safeMargin(config.marginX),
            marginY: safeMargin(config.marginY)
        };
    }

    function render() {
        const cfg = state.config;
        shadow.innerHTML = `
            <style>
                :host {
                    --bp-primary: ${cfg.primaryColor};
                    --bp-modal-bg: ${cfg.modalBackgroundColor};
                    --bp-modal-text: ${cfg.modalTextColor};
                    --bp-title-text: ${cfg.titleColor};
                    --bp-link-text: ${cfg.linkTextColor};
                    --bp-button-size: ${cfg.buttonSize}px;
                    --bp-margin-x: ${cfg.marginX}px;
                    --bp-margin-y: ${cfg.marginY}px;
                    --bp-text: var(--bp-modal-text);
                    --bp-muted: #7b8290;
                    --bp-border: #dfe4ec;
                    --bp-soft: #f5f7fb;
                    font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                }
                * { box-sizing: border-box; }
                .bp-launcher {
                    position: fixed;
                    z-index: 2147483000;
                    border: 0;
                    border-radius: 999px;
                    color: var(--bp-primary);
                    font: 800 15px/1 Inter, ui-sans-serif, system-ui, sans-serif;
                    cursor: pointer;
                }
                .bp-launcher.is-button {
                    min-height: 48px;
                    width: var(--bp-button-size);
                    height: var(--bp-button-size);
                    padding: 0;
                    background: var(--bp-primary);
                    color: #fff;
                    box-shadow: 0 18px 42px rgba(24, 22, 58, 0.24);
                }
                .bp-launcher.is-link {
                    min-height: auto;
                    padding: 0;
                    background: transparent;
                    color: var(--bp-link-text);
                    box-shadow: none;
                    text-decoration: ${cfg.linkUnderline ? "underline" : "none"};
                    text-underline-offset: 3px;
                }
                .bp-launcher.bottom-right { right: var(--bp-margin-x); bottom: var(--bp-margin-y); }
                .bp-launcher.bottom-left { left: var(--bp-margin-x); bottom: var(--bp-margin-y); }
                .bp-launcher.top-right { right: var(--bp-margin-x); top: var(--bp-margin-y); }
                .bp-launcher.top-left { left: var(--bp-margin-x); top: var(--bp-margin-y); }
                .bp-overlay {
                    position: fixed;
                    inset: 0;
                    z-index: 2147483001;
                    display: none;
                    align-items: center;
                    justify-content: center;
                    padding: 24px;
                    background: rgba(39, 33, 95, 0.58);
                    backdrop-filter: blur(3px);
                }
                .bp-overlay.is-open { display: flex; }
                .bp-modal {
                    width: min(760px, 100%);
                    border-radius: 18px;
                    background: var(--bp-modal-bg);
                    color: var(--bp-text);
                    box-shadow: 0 28px 90px rgba(16, 18, 32, 0.32);
                    padding: 28px;
                }
                .bp-header {
                    display: flex;
                    align-items: flex-start;
                    justify-content: space-between;
                    gap: 18px;
                    margin-bottom: 24px;
                }
                .bp-title {
                    margin: 0;
                    color: var(--bp-title-text);
                    font-size: 26px;
                    line-height: 1.15;
                    font-weight: 900;
                    letter-spacing: 0;
                }
                .bp-close {
                    width: 40px;
                    height: 40px;
                    border: 0;
                    border-radius: 10px;
                    background: var(--bp-soft);
                    color: var(--bp-muted);
                    font-size: 28px;
                    line-height: 1;
                    cursor: pointer;
                }
                .bp-success {
                    display: none;
                    padding: 22px 8px 10px;
                    text-align: left;
                }
                .bp-success.is-visible {
                    display: grid;
                    gap: 14px;
                }
                .bp-success strong {
                    display: block;
                    font-size: 22px;
                }
                .bp-success-url {
                    display: grid;
                    gap: 6px;
                    border-radius: 12px;
                    border: 1px solid rgba(255,255,255,.12);
                    background: rgba(255,255,255,.08);
                    padding: 12px;
                    overflow-wrap: anywhere;
                }
                .bp-success-url span {
                    color: var(--bp-muted);
                    font-size: 12px;
                    font-weight: 800;
                    text-transform: uppercase;
                }
                .bp-success-url a {
                    color: var(--bp-link-text);
                    font-weight: 900;
                    text-decoration: none;
                }
                .bp-success-cta {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    min-height: 42px;
                    border-radius: 10px;
                    background: var(--bp-primary);
                    color: #fff;
                    font-weight: 900;
                    text-decoration: none;
                    padding: 10px 14px;
                }
                .bp-fields {
                    display: grid;
                    gap: 16px;
                }
                .bp-fields.is-hidden { display: none; }
                .bp-label {
                    display: block;
                    margin-bottom: 8px;
                    color: var(--bp-text);
                    font-size: 14px;
                    font-weight: 800;
                    opacity: 0.76;
                }
                .bp-input,
                .bp-textarea {
                    width: 100%;
                    border: 1px solid var(--bp-border);
                    border-radius: 10px;
                    background: #fff;
                    color: var(--bp-text);
                    font: 500 16px/1.4 Inter, ui-sans-serif, system-ui, sans-serif;
                    outline: none;
                    transition: border-color 160ms ease, box-shadow 160ms ease;
                }
                .bp-input {
                    min-height: 54px;
                    padding: 0 16px;
                }
                .bp-textarea {
                    min-height: 132px;
                    resize: vertical;
                    padding: 14px 16px;
                }
                .bp-input:focus,
                .bp-textarea:focus {
                    border-color: var(--bp-primary);
                    box-shadow: 0 0 0 4px color-mix(in srgb, var(--bp-primary) 16%, transparent);
                }
                .bp-actions {
                    display: flex;
                    flex-wrap: wrap;
                    align-items: center;
                    justify-content: space-between;
                    gap: 12px;
                    margin-top: 6px;
                }
                .bp-button {
                    min-height: 48px;
                    border: 0;
                    border-radius: 10px;
                    padding: 0 18px;
                    background: var(--bp-primary);
                    color: #fff;
                    font: 900 15px/1 Inter, ui-sans-serif, system-ui, sans-serif;
                    cursor: pointer;
                }
                .bp-button[disabled] {
                    cursor: not-allowed;
                    background: #e1e5ea;
                    color: #9aa3ad;
                }
                .bp-help {
                    margin: 0;
                    color: var(--bp-muted);
                    font-size: 14px;
                    line-height: 1.5;
                }
                .bp-honeypot,
                .bp-hidden { display: none; }
                @media (max-width: 640px) {
                    .bp-overlay { padding: 12px; }
                    .bp-modal {
                        max-height: calc(100vh - 24px);
                        overflow: auto;
                        border-radius: 14px;
                        padding: 20px;
                    }
                    .bp-title { font-size: 22px; }
                }
            </style>
            ${cfg.buttonStyle === "custom" ? "" : `<button class="bp-launcher ${escapeHtml(safePosition(cfg.position))} is-${escapeHtml(cfg.buttonStyle)}" type="button" title="${escapeHtml(cfg.buttonText)}" aria-label="${escapeHtml(cfg.buttonText)}" data-bp-open>${cfg.buttonStyle === "link" ? escapeHtml(cfg.buttonText) : "!"}</button>`}
            <div class="bp-overlay" data-bp-overlay>
                <form class="bp-modal" method="post" action="${baseUrl}/api/user/modalControl" data-bp-form>
                    <div class="bp-header">
                        <h2 class="bp-title">${escapeHtml(cfg.title)}</h2>
                        <button class="bp-close" type="button" aria-label="Fermer" data-bp-close>&times;</button>
                    </div>
                    <div class="bp-success" data-bp-success>
                        <strong>Merci pour votre retour.</strong>
                        <span>Votre rapport a bien été transmis.</span>
                        <div class="bp-success-url">
                            <span>Lien signalé</span>
                            <a href="#" target="_blank" rel="noopener" data-bp-success-url></a>
                        </div>
                        <a class="bp-success-cta" href="${baseUrl}/pollUser" target="_blank" rel="noopener">Nous noter en 3 questions</a>
                    </div>
                    <div class="bp-fields" data-bp-fields>
                        <label>
                            <span class="bp-label">URL concernee</span>
                            <input class="bp-input" type="text" name="url" data-bp-url readonly>
                        </label>
                        <div>
                            <button class="bp-button" type="button" data-bp-pointer>Pointer le bug</button>
                            <p class="bp-help" data-bp-help>Selectionnez l'endroit de la page qui contient le probleme.</p>
                        </div>
                        <textarea class="bp-hidden" name="codeLocation" data-bp-code readonly></textarea>
                        <label>
                            <span class="bp-label">${escapeHtml(cfg.descriptionLabel)}</span>
                            <textarea class="bp-textarea" name="description" minlength="5" required placeholder="Expliquez le probleme rencontre" data-bp-description></textarea>
                        </label>
                        ${cfg.collectEmail ? `
                        <label>
                            <span class="bp-label">Votre e-mail</span>
                            <input class="bp-input" type="email" name="mail" required placeholder="vous@exemple.fr">
                            <span class="bp-help">Recevez le rapport de test sans créer de compte.</span>
                        </label>
                        ` : ""}
                        <input class="bp-honeypot" type="text" name="bot" tabindex="-1" autocomplete="off">
                        <input type="hidden" name="os" data-bp-os>
                        <input type="hidden" name="browser" data-bp-browser>
                        <input type="hidden" name="screenSize" data-bp-screen>
                        <input type="hidden" name="key" value="${escapeHtml(publicKey)}">
                        <div class="bp-actions">
                            <p class="bp-help">bugspointer.com</p>
                            <button class="bp-button" type="submit" data-bp-submit disabled>Selectionnez le bug</button>
                        </div>
                    </div>
                </form>
            </div>
        `;
    }

    function bindEvents() {
        const launcher = shadow.querySelector("[data-bp-open]");
        const overlay = shadow.querySelector("[data-bp-overlay]");
        const close = shadow.querySelector("[data-bp-close]");
        const pointer = shadow.querySelector("[data-bp-pointer]");
        const form = shadow.querySelector("[data-bp-form]");
        const submit = shadow.querySelector("[data-bp-submit]");
        const description = shadow.querySelector("[data-bp-description]");

        if (launcher) {
            launcher.addEventListener("click", open);
        }
        bindCustomTriggers(open);
        close.addEventListener("click", closeModal);
        pointer.addEventListener("click", startPointer);
        description.addEventListener("input", validateSubmit);
        form.addEventListener("submit", submitReport);
        overlay.addEventListener("click", (event) => {
            if (event.target === overlay) closeModal();
        });

        function open() {
            shadow.querySelector("[data-bp-url]").value = window.location.href;
            shadow.querySelector("[data-bp-os]").value = getPlatform();
            shadow.querySelector("[data-bp-browser]").value = getBrowser();
            shadow.querySelector("[data-bp-screen]").value = `${window.innerWidth} x ${window.innerHeight}`;
            overlay.classList.add("is-open");
        }

        function closeModal() {
            overlay.classList.remove("is-open");
            stopPointer();
        }

        function submitReport(event) {
            event.preventDefault();
            if (!validateSubmit()) {
                return;
            }
            submit.disabled = true;
            submit.textContent = "Envoi en cours...";

            fetch(form.action, {
                method: form.method,
                body: new FormData(form),
                mode: "no-cors",
                credentials: "omit"
            }).finally(() => {
                const currentUrl = shadow.querySelector("[data-bp-url]").value || window.location.href;
                const successUrl = shadow.querySelector("[data-bp-success-url]");
                successUrl.href = currentUrl;
                successUrl.textContent = currentUrl;
                shadow.querySelector("[data-bp-fields]").classList.add("is-hidden");
                shadow.querySelector("[data-bp-success]").classList.add("is-visible");
            });
        }

        function startPointer() {
            closeModal();
            state.pointing = true;
            document.addEventListener("mouseover", highlight, true);
            document.addEventListener("mouseout", unhighlight, true);
            document.addEventListener("click", selectElement, true);
        }

        function stopPointer() {
            state.pointing = false;
            restoreSelectedBorder();
            document.removeEventListener("mouseover", highlight, true);
            document.removeEventListener("mouseout", unhighlight, true);
            document.removeEventListener("click", selectElement, true);
        }

        function highlight(event) {
            if (!state.pointing || host.contains(event.target)) {
                return;
            }
            restoreSelectedBorder();
            state.selectedElement = event.target;
            state.selectedBorder = event.target.style.border;
            event.target.style.border = "3px solid #FF4D4F";
        }

        function unhighlight(event) {
            if (!state.pointing || event.target !== state.selectedElement) {
                return;
            }
            restoreSelectedBorder();
        }

        function selectElement(event) {
            if (!state.pointing || host.contains(event.target)) {
                return;
            }
            event.preventDefault();
            event.stopPropagation();
            const element = event.target;
            const parent = findReadableParent(element);
            shadow.querySelector("[data-bp-code]").value = markSelectedElement(parent.outerHTML, element.outerHTML);
            shadow.querySelector("[data-bp-help]").textContent = "Bug pointé correctement.";
            pointer.textContent = "Bug pointé correctement";
            pointer.style.background = "#00E676";
            pointer.style.color = "#06100c";
            state.pointed = true;
            validateSubmit();
            stopPointer();
            open();
        }

        function validateSubmit() {
            const isValid = state.pointed && description.value.trim().length >= 5;
            submit.disabled = !isValid;
            submit.textContent = state.pointed ? "Envoyer le rapport" : "Sélectionnez le bug";
            if (state.pointed && !isValid) {
                submit.textContent = "Décrivez le bug";
            }
            return isValid;
        }
    }

    function bindCustomTriggers(open) {
        const triggers = document.querySelectorAll("[data-bugspointer-open]");
        triggers.forEach((trigger) => {
            if (trigger.dataset.bugspointerBound === "true") {
                return;
            }
            trigger.dataset.bugspointerBound = "true";
            if (!trigger.textContent.trim()) {
                trigger.textContent = state.config.buttonText;
            }
            if (trigger.dataset.bugspointerUnstyled !== "true") {
                trigger.style.color = state.config.linkTextColor;
                trigger.style.cursor = "pointer";
                trigger.style.fontWeight = "800";
                trigger.style.textDecoration = state.config.linkUnderline ? "underline" : "none";
                trigger.style.textUnderlineOffset = "3px";
            }
            trigger.addEventListener("click", (event) => {
                event.preventDefault();
                open();
            });
        });
    }

    function restoreSelectedBorder() {
        if (state.selectedElement) {
            state.selectedElement.style.border = state.selectedBorder;
            state.selectedElement = null;
            state.selectedBorder = "";
        }
    }

    function findReadableParent(element) {
        const readableContainers = ["ARTICLE", "ASIDE", "SECTION", "HEADER", "FOOTER", "NAV", "LI", "TR", "FORM"];
        let readable = element;

        while (readable.parentElement && readable.parentElement !== document.body) {
            const parent = readable.parentElement;
            if (readableContainers.includes(parent.tagName)) {
                return parent;
            }
            if (parent.tagName === "MAIN") {
                break;
            }
            readable = parent;
        }

        return readable;
    }

    function markSelectedElement(parentHtml, elementHtml) {
        let selected = elementHtml;
        const tag = selected.match(/^<([a-z0-9-]+)/i);
        if (selected.includes('class="')) {
            selected = selected.replace('class="', 'class="bugspointer-pointed-balise ');
        } else if (tag) {
            selected = selected.replace(`<${tag[1]}`, `<${tag[1]} class="bugspointer-pointed-balise"`);
        }
        return parentHtml.replace(elementHtml, selected).replace(/ style="[^"]*"/g, "");
    }

    function getPlatform() {
        if (navigator.userAgentData && navigator.userAgentData.platform) {
            return navigator.userAgentData.platform;
        }
        return navigator.platform || "unknown";
    }

    function getBrowser() {
        if (navigator.userAgentData && navigator.userAgentData.brands && navigator.userAgentData.brands.length > 0) {
            const brand = navigator.userAgentData.brands[0];
            return `${brand.brand} v${brand.version}`;
        }

        const userAgent = navigator.userAgent.toLowerCase();
        const browsers = {
            edge: /edge\/([\d.]+)/.exec(userAgent),
            chrome: /chrome\/([\d.]+)/.exec(userAgent),
            safari: /version\/([\d.]+).*safari/.exec(userAgent),
            firefox: /firefox\/([\d.]+)/.exec(userAgent),
            ie: /msie ([\d.]+)/.exec(userAgent) || /trident\/.*rv:([\d.]+)/.exec(userAgent)
        };

        for (const browser in browsers) {
            if (browsers[browser]) {
                return `${browser.charAt(0).toUpperCase()}${browser.slice(1)} v${browsers[browser][1]}`;
            }
        }
        return "unknown";
    }

    function safePosition(position) {
        return ["bottom-right", "bottom-left", "top-right", "top-left"].includes(position) ? position : "bottom-right";
    }

    function safeButtonStyle(style) {
        return ["button", "custom"].includes(style) ? style : "button";
    }

    function safeColor(color, fallback = defaults.primaryColor) {
        return /^#[0-9a-fA-F]{6}$/.test(color || "") ? color : fallback;
    }

    function safeMargin(margin) {
        const parsed = Number.parseInt(margin, 10);
        if (Number.isNaN(parsed)) {
            return 15;
        }
        return Math.min(Math.max(parsed, 0), 120);
    }

    function safeButtonSize(size) {
        const parsed = Number.parseInt(size, 10);
        if (Number.isNaN(parsed)) {
            return defaults.buttonSize;
        }
        return Math.min(Math.max(parsed, 44), 96);
    }

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
})();
