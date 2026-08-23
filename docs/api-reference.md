# Enterprise NVR API Reference

## Authentication

POST /api/auth/login

## Device

GET /api/device/list

## Camera

GET /api/camera/list

## Stream

POST /api/stream/start/{cameraId}

## Playback

POST /api/playback/start

Request:

```json
{
  "cameraId":1,
  "startTime":"2026-01-01 10:00:00",
  "endTime":"2026-01-01 11:00:00"
}
```
