package studio.toska.ferbrows

import org.json.JSONObject

internal fun credentialAutofillScript(saved: SavedCredential?): String {
    val savedJson = if (saved == null) "null" else JSONObject().apply {
        put("username", saved.username)
        put("password", saved.password)
    }.toString()
    return """
      (function () {
        if (window.__ferBrowsVaultAutofillInstalled) return;
        window.__ferBrowsVaultAutofillInstalled = true;
        const saved = $savedJson;
        const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));
        const inputs = () => Array.from(document.querySelectorAll('input'))
          .filter(el => visible(el) && !el.disabled && !el.readOnly);
        const isPassword = el => (el.type || '').toLowerCase() === 'password';
        const excluded = el => /^(search|hidden|checkbox|radio|submit|button|reset|image|file)$/i.test(el.type || '');
        const identityText = el => [el.name, el.id, el.autocomplete, el.placeholder,
          el.getAttribute('aria-label'), el.getAttribute('data-testid')].filter(Boolean).join(' ').toLowerCase();
        const findUser = list => list.find(el => !isPassword(el) && !excluded(el) &&
          /(^|\b)(email|e-mail|identifier|username|user-name|login|account)(\b|$)/i.test(identityText(el))) ||
          list.find(el => !isPassword(el) && !excluded(el) &&
            ((el.type || '').toLowerCase() === 'email' || /^(email|username)$/i.test(el.autocomplete || '')));
        const setValue = (el, value) => {
          const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set;
          if (setter) setter.call(el, value); else el.value = value;
          el.dispatchEvent(new Event('input', {bubbles:true}));
          el.dispatchEvent(new Event('change', {bubbles:true}));
        };
        const fill = () => {
          if (!saved) return;
          const list = inputs();
          const pass = list.find(isPassword);
          const user = findUser(list);
          if (user && !user.value && saved.username) setValue(user, saved.username);
          if (pass && !pass.value && saved.password) setValue(pass, saved.password);
        };
        const observer = new MutationObserver(fill);
        const start = () => {
          fill();
          if (document.documentElement) observer.observe(document.documentElement, {childList:true, subtree:true});
        };
        if (document.documentElement) start();
        else document.addEventListener('DOMContentLoaded', start, {once:true});
      })();
    """.trimIndent()
}
