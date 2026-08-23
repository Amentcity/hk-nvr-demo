package com.amentcity.nvr.device.hikvision;

import java.util.List;

public interface HikvisionService {

    long login(String ip,int port,String username,String password);

    List<?> getChannels(long userId);

    void logout(long userId);
}
