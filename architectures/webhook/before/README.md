# Before: Polling-Based Partner Integration

## What you are looking at

`PartnerIntegrationService` runs a continuous polling loop. Every 5 seconds it calls `GET /payments/{id}/status`
for every tracked order. When a status change is detected it dispatches to each hard-coded partner handler.

## Smells to notice

1. **Wasteful:** ~99.9 % of HTTP calls return "no change."
2. **Latency:** partners find out up to 5 s late → not "the instant it happened."
3. **Closed to extension:** adding a fourth partner means opening this class and adding another `if`/method call.
4. **No security:** nothing proves these status calls came from the real gateway.
5. **Thread management leak:** the polling loop runs on a raw thread → no backpressure, no graceful shutdown.