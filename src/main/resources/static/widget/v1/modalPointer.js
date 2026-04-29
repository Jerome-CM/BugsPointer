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
        buttonText: "Signaler un bug",
        title: "Signaler un nouveau bug",
        descriptionLabel: "Description du bug",
        position: "bottom-right"
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
        if (script.dataset.buttonText) config.buttonText = script.dataset.buttonText;
        if (script.dataset.title) config.title = script.dataset.title;
        if (script.dataset.descriptionLabel) config.descriptionLabel = script.dataset.descriptionLabel;
        if (script.dataset.position) config.position = script.dataset.position;
        return config;
    }

    function normalizeConfig(config) {
        return {
            primaryColor: safeColor(config.primaryColor),
            buttonText: config.buttonText || defaults.buttonText,
            title: config.title || defaults.title,
            descriptionLabel: config.descriptionLabel || defaults.descriptionLabel,
            position: safePosition(config.position)
        };
    }

    function render() {
        const cfg = state.config;
        shadow.innerHTML = `
            <style>
                :host {
                    --bp-primary: ${cfg.primaryColor};
                    --bp-text: #24233d;
                    --bp-muted: #7b8290;
                    --bp-border: #dfe4ec;
                    --bp-soft: #f5f7fb;
                    font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                }
                * { box-sizing: border-box; }
                .bp-launcher {
                    position: fixed;
                    z-index: 2147483000;
                    min-height: 48px;
                    border: 0;
                    border-radius: 10px;
                    padding: 0 18px;
                    background: var(--bp-primary);
                    color: #fff;
                    font: 800 15px/1 Inter, ui-sans-serif, system-ui, sans-serif;
                    box-shadow: 0 18px 42px rgba(24, 22, 58, 0.24);
                    cursor: pointer;
                }
                .bp-launcher.bottom-right { right: 20px; bottom: 20px; }
                .bp-launcher.bottom-left { left: 20px; bottom: 20px; }
                .bp-launcher.top-right { right: 20px; top: 20px; }
                .bp-launcher.top-left { left: 20px; top: 20px; }
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
                    background: #fff;
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
                    padding: 28px 8px 10px;
                    text-align: center;
                }
                .bp-success.is-visible { display: block; }
                .bp-success strong {
                    display: block;
                    margin-bottom: 10px;
                    font-size: 22px;
                }
                .bp-fields {
                    display: grid;
                    gap: 16px;
                }
                .bp-fields.is-hidden { display: none; }
                .bp-label {
                    display: block;
                    margin-bottom: 8px;
                    color: #4d5663;
                    font-size: 14px;
                    font-weight: 800;
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
                    .bp-launcher {
                        right: 14px;
                        bottom: 14px;
                        left: auto;
                        top: auto;
                    }
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
            <button class="bp-launcher ${escapeHtml(safePosition(cfg.position))}" type="button" data-bp-open>${escapeHtml(cfg.buttonText)}</button>
            <div class="bp-overlay" data-bp-overlay>
                <form class="bp-modal" method="post" action="${baseUrl}/api/user/modalControl" data-bp-form>
                    <div class="bp-header">
                        <h2 class="bp-title">${escapeHtml(cfg.title)}</h2>
                        <button class="bp-close" type="button" aria-label="Fermer" data-bp-close>&times;</button>
                    </div>
                    <div class="bp-success" data-bp-success>
                        <strong>Merci pour votre retour.</strong>
                        <span>Votre rapport a bien ete transmis.</span>
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
                            <textarea class="bp-textarea" name="description" minlength="10" required placeholder="Expliquez le probleme rencontre"></textarea>
                        </label>
                        <input class="bp-honeypot" type="text" name="bot" tabindex="-1" autocomplete="off">
                        <input type="hidden" name="os" data-bp-os>
                        <input type="hidden" name="browser" data-bp-browser>
                        <input type="hidden" name="screenSize" data-bp-screen>
                        <input type="hidden" name="key" value="${escapeHtml(publicKey)}">
                        <div class="bp-actions">
                            <p class="bp-help">Le rapport inclut le navigateur et la taille d'ecran.</p>
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

        launcher.addEventListener("click", open);
        close.addEventListener("click", closeModal);
        pointer.addEventListener("click", startPointer);
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
            if (!state.pointed) {
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
            shadow.querySelector("[data-bp-help]").textContent = "Bug pointe correctement.";
            pointer.textContent = "Bug pointe correctement";
            state.pointed = true;
            submit.disabled = false;
            submit.textContent = "Envoyer le rapport";
            stopPointer();
            open();
        }
    }

    function restoreSelectedBorder() {
        if (state.selectedElement) {
            state.selectedElement.style.border = state.selectedBorder;
            state.selectedElement = null;
            state.selectedBorder = "";
        }
    }

    function findReadableParent(element) {
        let readable = element;
        for (let index = 0; index < 2; index += 1) {
            if (!readable.parentElement || readable.parentElement === document.body) {
                break;
            }
            readable = readable.parentElement;
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

    function safeColor(color) {
        return /^#[0-9a-fA-F]{6}$/.test(color || "") ? color : defaults.primaryColor;
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
