package com.glt.magikoly.config;

/**
 * Created by kingyang on 2016/7/26.
 */
public class ConfigBeanFactory {

    public static AbsConfigBean getConfigBean(int sid) {
        switch (sid) {
            case AgingShutterConfigBean.SID:
                return new AgingShutterConfigBean();
            case DiscoverySearchConfigBean.SID:
                return new DiscoverySearchConfigBean();
            case InnerAdConfigBean.SID:
                return new InnerAdConfigBean();
            case FilterSortConfigBean.SID:
                return new FilterSortConfigBean();
            case MainBannerConfigBean.SID:
                return new MainBannerConfigBean();
            case AnimalConfigBean.SID:
                return new AnimalConfigBean();
        }
        return null;
    }
}
