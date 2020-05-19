package com.netflix.appinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import com.netflix.discovery.CommonConstants;
import com.netflix.discovery.shared.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract instance info configuration with some defaults to get the users
 * started quickly.The users have to override only a few methods to register
 * their instance with eureka server.
 *
 * @author Karthik Ranganathan
 *
 */
public abstract class AbstractInstanceConfig implements EurekaInstanceConfig {
    private static final Logger logger = LoggerFactory.getLogger(AbstractInstanceConfig.class);

    /**
     * @deprecated 2016-08-29 use {@link CommonConstants#DEFAULT_CONFIG_NAMESPACE}
     */
    @Deprecated
    public static final String DEFAULT_NAMESPACE = CommonConstants.DEFAULT_CONFIG_NAMESPACE;
    /**
     * 契约过期时间，单位：秒
     */
    private static final int LEASE_EXPIRATION_DURATION_SECONDS = 90;
    /**
     * 租约续约频率，单位：秒。
     */
    private static final int LEASE_RENEWAL_INTERVAL_SECONDS = 30;
    /**
     * 应用 https 端口关闭
     */
    private static final boolean SECURE_PORT_ENABLED = false;
    /**
     * 应用 http 端口开启
     */
    private static final boolean NON_SECURE_PORT_ENABLED = true;
    /**
     * 应用 http 端口
     */

    private static final int NON_SECURE_PORT = 80;
    /**
     * 应用 https 端口
     */
    private static final int SECURE_PORT = 443;
    /**
     * 应用初始化后开启
     */
    private static final boolean INSTANCE_ENABLED_ON_INIT = false;
    /**
     * 主机信息
     * key：主机 IP 地址
     * value：主机名
     */
    private static final Pair<String, String> hostInfo = getHostInfo();
    private DataCenterInfo info = new DataCenterInfo() {
        @Override
        public Name getName() {
            return Name.MyOwn;
        }
    };

    protected AbstractInstanceConfig() {

    }

    protected AbstractInstanceConfig(DataCenterInfo info) {
        this.info = info;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#isInstanceEnabledOnit()
     */
    @Override
    public boolean isInstanceEnabledOnit() {
        return INSTANCE_ENABLED_ON_INIT;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getNonSecurePort()
     */
    @Override
    public int getNonSecurePort() {
        return NON_SECURE_PORT;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getSecurePort()
     */
    @Override
    public int getSecurePort() {
        return SECURE_PORT;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#isNonSecurePortEnabled()
     */
    @Override
    public boolean isNonSecurePortEnabled() {
        return NON_SECURE_PORT_ENABLED;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getSecurePortEnabled()
     */
    @Override
    public boolean getSecurePortEnabled() {
        // TODO Auto-generated method stub
        return SECURE_PORT_ENABLED;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.netflix.appinfo.InstanceConfig#getLeaseRenewalIntervalInSeconds()
     */
    @Override
    public int getLeaseRenewalIntervalInSeconds() {
        return LEASE_RENEWAL_INTERVAL_SECONDS;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.netflix.appinfo.InstanceConfig#getLeaseExpirationDurationInSeconds()
     */
    @Override
    public int getLeaseExpirationDurationInSeconds() {
        return LEASE_EXPIRATION_DURATION_SECONDS;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getVirtualHostName()
     */
    @Override
    public String getVirtualHostName() {
        return (getHostName(false) + ":" + getNonSecurePort());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getSecureVirtualHostName()
     */
    @Override
    public String getSecureVirtualHostName() {
        return (getHostName(false) + ":" + getSecurePort());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getASGName()
     */
    @Override
    public String getASGName() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getHostName()
     */
    @Override
    public String getHostName(boolean refresh) {
        return hostInfo.second();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getMetadataMap()
     */
    @Override
    public Map<String, String> getMetadataMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getDataCenterInfo()
     */
    /**
     * 数据中心信息
     */
    @Override
    public DataCenterInfo getDataCenterInfo() {
        // TODO Auto-generated method stub
        return info;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.netflix.appinfo.InstanceConfig#getIpAddress()
     */
    @Override
    public String getIpAddress() {
        return hostInfo.first();
    }

    public boolean shouldBroadcastPublicIpv4Addr () { return false; }

    /**
     * 获取本地服务器的主机名和主机 IP 地址。如果主机有多网卡或者虚拟机网卡，这块要小心，解决方式如下：
     * 手动配置本机的 hostname + etc/hosts 文件，从而映射主机名和 IP 地址。
     * 使用 Spring-Cloud-Eureka-Client 的话，参考周立 —— 《Eureka服务注册过程详解之IpAddress》解决
     * @return
     */
    private static Pair<String, String> getHostInfo() {
        Pair<String, String> pair;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            pair = new Pair<String, String>(localHost.getHostAddress(), localHost.getHostName());
        } catch (UnknownHostException e) {
            logger.error("Cannot get host info", e);
            pair = new Pair<String, String>("", "");
        }
        return pair;
    }

}
