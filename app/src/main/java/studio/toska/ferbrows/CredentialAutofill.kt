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
        const storageKey = '__ferbrows_pending_username';
        let lastUsername = '';
        let lastPassword = '';
        let lastSent = '';
        let offerTimer = 0;

        const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));
        const inputs = root => Array.from((root || document).querySelectorAll('input'))
          .filter(el => visible(el) && !el.disabled && !el.readOnly);
        const isPassword = el => (el.type || '').toLowerCase() === 'password';
        const isExcluded = el => /^(search|hidden|checkbox|radio|submit|button|reset|image|file)$/i.test(el.type || '');
        const identityText = el => [
          el.name, el.id, el.autocomplete, el.placeholder,
          el.getAttribute('aria-label'), el.getAttribute('data-testid')
        ].filter(Boolean).join(' ').toLowerCase();

        const usernameCandidate = list =>
          list.find(el => !isPassword(el) && !isExcluded(el) &&
            /(^|\\b)(email|e-mail|username|user-name|login|account)(\\b|$)/i.test(identityText(el))) ||
          list.find(el => !isPassword(el) && !isExcluded(el) &&
            ((el.type || '').toLowerCase() === 'email' || /^(email|username)$/i.test(el.autocomplete || ''))) ||
          list.find(el => !isPassword(el) && !isExcluded(el) &&
            ['text', ''].includes((el.type || 'text').toLowerCase()));

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
          if (user && user.value.trim()) {
            lastUsername = user.value.trim();
            try { sessionStorage.setItem(storageKey, lastUsername); } catch (_) {}
          }
          if (pass && pass.value) lastPassword = pass.value;
          if (!lastUsername) {
            try { lastUsername = sessionStorage.getItem(storageKey) || ''; } catch (_) {}
          }
          return { user, pass, username: lastUsername, password: lastPassword };
        };

        const tryAutofill = () => {
          if (!saved || window.__ferBrowsVaultFilled) return;
          const {user, pass} = collect();
          if (pass) {
            if (user && !user.value) setValue(user, saved.username);
            if (!pass.value) setValue(pass, saved.password);
            window.__ferBrowsVaultFilled = true;
          } else if (user && !user.value &&
              (/email|username|login/i.test(identityText(user)) || (user.type || '').toLowerCase() === 'email')) {
            setValue(user, saved.username);
          }
        };

        const sendCandidate = () => {
          const {username, password} = collect();
          if (!username || !password || !window.FerBrowsVault) return;
          const key = username + '\\n' + password;
          if (key === lastSent) return;
          lastSent = key;
          window.FerBrowsVault.postMessage(JSON.stringify({
            type: 'save', origin: location.origin, username, password
          }));
        };

        // Offer after the user has finished filling both the email/login and password.
        // A short pause avoids showing a prompt on every keystroke.
        const scheduleOffer = () => {
          const {username, password} = collect();
          if (!username || !password) return;
          if (offerTimer) clearTimeout(offerTimer);
          offerTimer = setTimeout(sendCandidate, 850);
        };

        document.addEventListener('input', event => {
          const el = event.target;
          if (!(el instanceof HTMLInputElement)) return;
          if (isPassword(el)) {
            lastPassword = el.value;
            scheduleOffer();
          } else if (!isExcluded(el) && el.value && !isPassword(el)) {
            const text = identityText(el);
            if (/email|e-mail|username|login|account|user/i.test(text) ||
                (el.type || '').toLowerCase() === 'email') {
              lastUsername = el.value.trim();
              try { sessionStorage.setItem(storageKey, lastUsername); } catch (_) {}
              scheduleOffer();
            }
          }
          tryAutofill();
        }, true);

        document.addEventListener('change', event => {
          const el = event.target;
          if (el instanceof HTMLInputElement && (isPassword(el) || /email|e-mail|username|login|account|user/i.test(identityText(el)))) {
            scheduleOffer();
          }
        }, true);
        document.addEventListener('submit', () => {
          if (offerTimer) clearTimeout(offerTimer);
          sendCandidate();
        }, true);
        document.addEventListener('click', event => {
          const target = event.target && event.target.closest
            ? event.target.closest('button, input[type="submit"], [role="button"]') : null;
          if (target) {
            if (offerTimer) clearTimeout(offerTimer);
            sendCandidate();
          }
        }, true);
        document.addEventListener('keydown', event => {
          if (event.key === 'Enter') {
            if (offerTimer) clearTimeout(offerTimer);
            sendCandidate();
          }
        }, true);

        const observer = new MutationObserver(() => tryAutofill());
        const start = () => {
          try {
            const previous = sessionStorage.getItem(storageKey);
            if (previous) lastUsername = previous;
          } catch (_) {}
          tryAutofill();
          if (document.documentElement) observer.observe(document.documentElement, {childList:true,subtree:true});
        };
        if (document.documentElement) start();
        else document.addEventListener('DOMContentLoaded', start, {once:true});
      })();
    """.trimIndent()
}
