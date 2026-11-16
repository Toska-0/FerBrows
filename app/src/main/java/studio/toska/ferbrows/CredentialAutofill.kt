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
        let lastUsername = '';
        let lastPassword = '';
        let lastSent = '';
        const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));
        const inputs = root => Array.from((root || document).querySelectorAll('input')).filter(visible);
        const isPassword = el => (el.type || '').toLowerCase() === 'password';
        const usernameCandidate = list => list.find(el => !isPassword(el) && /user|login|email|account/i.test((el.name || '') + ' ' + (el.id || '') + ' ' + (el.autocomplete || '') + ' ' + (el.placeholder || '')))
          || list.find(el => !isPassword(el) && (el.type || 'text') === 'email')
          || list.find(el => !isPassword(el) && ['text',''].includes((el.type || '').toLowerCase()));
        const setValue = (el, value) => {
          const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set;
          if (setter) setter.call(el, value); else el.value = value;
          el.dispatchEvent(new Event('input', { bubbles: true }));
          el.dispatchEvent(new Event('change', { bubbles: true }));
        };
        const collect = () => {
          const list = inputs(document);
          const pass = list.find(isPassword);
          const user = usernameCandidate(list);
          if (user && user.value) lastUsername = user.value.trim();
          if (pass && pass.value) lastPassword = pass.value;
          return { user, pass };
        };
        const tryAutofill = () => {
          if (!saved || window.__ferBrowsVaultFilled) return;
          const {user, pass} = collect();
          if (pass) {
            if (user && !user.value) setValue(user, saved.username);
            if (!pass.value) setValue(pass, saved.password);
            window.__ferBrowsVaultFilled = true;
          } else if (user && !user.value && /email|username|login/i.test((user.autocomplete || '') + ' ' + (user.name || '') + ' ' + (user.id || ''))) {
            setValue(user, saved.username);
          }
        };
        const maybeSave = () => {
          const {user, pass} = collect();
          const username = (user && user.value ? user.value.trim() : lastUsername);
          const password = pass && pass.value ? pass.value : lastPassword;
          if (!username || !password || !window.FerBrowsVault) return;
          const key = username + '\\n' + password;
          if (key === lastSent) return;
          lastSent = key;
          window.FerBrowsVault.postMessage(JSON.stringify({
            type: 'save', origin: location.origin, username, password
          }));
        };
        document.addEventListener('input', event => {
          const el = event.target;
          if (!(el instanceof HTMLInputElement)) return;
          if (isPassword(el)) lastPassword = el.value;
          else if (el.value && !/search|hidden|checkbox|radio|submit|button/i.test(el.type || 'text')) lastUsername = el.value.trim();
          tryAutofill();
        }, true);
        document.addEventListener('change', maybeSave, true);
        document.addEventListener('submit', maybeSave, true);
        document.addEventListener('click', event => {
          const target = event.target && event.target.closest ? event.target.closest('button, input[type="submit"], [role="button"]') : null;
          if (target) maybeSave();
        }, true);
        document.addEventListener('keydown', event => {
          if (event.key === 'Enter') maybeSave();
        }, true);
        const observer = new MutationObserver(() => tryAutofill());
        const start = () => {
          tryAutofill();
          if (document.documentElement) observer.observe(document.documentElement, {childList:true,subtree:true});
        };
        if (document.documentElement) start();
        else document.addEventListener('DOMContentLoaded', start, {once:true});
      })();
    """.trimIndent()
}
