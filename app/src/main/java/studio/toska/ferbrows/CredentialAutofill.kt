package studio.toska.ferbrows

import org.json.JSONObject

internal fun credentialAutofillScript(saved: SavedCredential?): String {
    val savedJson = if (saved == null) "null" else JSONObject().apply {
        put("username", saved.username)
        put("password", saved.password)
    }.toString()
    return """
        (function () {
          if (window.__ferBrowsVaultInstalled) return;
          window.__ferBrowsVaultInstalled = true;
          const saved = $savedJson;
          const visible = (el) => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));
          const usernameField = (form) => {
            const inputs = Array.from(form.querySelectorAll('input:not([type="hidden"]):not([type="password"])')).filter(visible);
            return inputs.find(el => /user|login|email|account/i.test((el.name || '') + ' ' + (el.id || '') + ' ' + (el.autocomplete || '')))
              || inputs.find(el => (el.type || 'text') === 'email')
              || inputs.find(el => (el.type || 'text') === 'text');
          };
          const passwordFields = (form) => Array.from(form.querySelectorAll('input[type="password"]')).filter(visible);
          const setValue = (el, value) => {
            const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set;
            if (setter) setter.call(el, value); else el.value = value;
            el.dispatchEvent(new Event('input', { bubbles: true }));
            el.dispatchEvent(new Event('change', { bubbles: true }));
          };
          const scan = () => {
            document.querySelectorAll('form').forEach(form => {
              if (form.dataset.ferBrowsVaultBound === '1') return;
              form.dataset.ferBrowsVaultBound = '1';
              form.addEventListener('submit', () => {
                const passwords = passwordFields(form);
                const user = usernameField(form);
                const password = passwords[0];
                if (password && user && user.value && password.value && window.FerBrowsVault) {
                  window.FerBrowsVault.postMessage(JSON.stringify({
                    type: 'save',
                    origin: location.origin,
                    username: user.value,
                    password: password.value
                  }));
                }
              }, true);
            });
            if (saved && !window.__ferBrowsVaultFilled) {
              const forms = Array.from(document.querySelectorAll('form'));
              for (const form of forms) {
                const passwords = passwordFields(form);
                const user = usernameField(form);
                if (passwords.length === 1 && passwords[0] && user) {
                  if (!user.value) setValue(user, saved.username);
                  if (!passwords[0].value) setValue(passwords[0], saved.password);
                  window.__ferBrowsVaultFilled = true;
                  break;
                }
              }
            }
          };
          scan();
          document.addEventListener('DOMContentLoaded', scan, { once: true });
          new MutationObserver(scan).observe(document.documentElement, { childList: true, subtree: true });
        })();
    """.trimIndent()
}
