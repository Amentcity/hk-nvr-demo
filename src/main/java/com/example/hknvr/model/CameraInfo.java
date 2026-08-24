package com.example.hknvr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified camera model.
 *
 * This model is used as the common data structure between:
 * - Hikvision NVR device tree
 * - RTSP video service
 * - Digital twin integration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraInfo {

    /**
     * Unique camera id.
     * Recommended format: {nvrId}_CH{channelNo}
     */
    private String id;

    /** Camera display name */
    private String name;

    /** NVR identifier / serial number */
    private String nvrId;

    /** Physical NVR channel number */
    private int channel;

    /** Camera IP address */
    private String ip;

    /** Channel type: ANALOG / IP */
    private String type;

    /** Main stream RTSP address */
    private String mainStreamUrl;

    /** Sub stream RTSP address */
    private String subStreamUrl;

    /** Device online status */
    private boolean online;

    /**
     * Digital twin binding fields.
     * Reserved for building/floor/room mapping.
     */
    private String buildingId;
    private String floorId;
    private String roomId;
}
