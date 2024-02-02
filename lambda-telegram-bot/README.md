# Telegram Bot Lambda

Responds to hook events from Telegram and sends a message back to the user.

## Install the Telegram Hook

First, check that bot is accessible:
```bash
curl https://api.telegram.org/bot<token>/getMe
```

Then, set the webhook:
```bash
curl -X POST https://api.telegram.org/bot<token>/setWebhook \
-H 'Content-Type: application/json' \
-d "{\"url\": \"<url>\", \"secret_token\": \"<secret_token>\"}"
```

Check that hook is installed:
```bash
curl https://api.telegram.org/bot<token>/getWebhookInfo
```
