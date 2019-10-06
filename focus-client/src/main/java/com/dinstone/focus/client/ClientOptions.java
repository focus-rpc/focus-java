package com.dinstone.focus.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dinstone.focus.endpoint.EndpointOptions;
import com.dinstone.focus.filter.Filter;
import com.dinstone.photon.ConnectOptions;

public class ClientOptions extends EndpointOptions {

    private List<InetSocketAddress> serviceAddresses = new ArrayList<>();

    private ConnectOptions connectOptions = new ConnectOptions();

    private List<Filter> filters = new ArrayList<>();

    private int connectPoolSize = 2;

    public ConnectOptions getConnectOptions() {
        return connectOptions;
    }

    public void setConnectOptions(ConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
    }

    public ClientOptions bind(String addresses) {
        if (addresses == null || addresses.length() == 0) {
            return this;
        }

        String[] addressArrays = addresses.split(",");
        for (String address : addressArrays) {
            int pidx = address.lastIndexOf(':');
            if (pidx > 0 && (pidx < address.length() - 1)) {
                String host = address.substring(0, pidx);
                int port = Integer.parseInt(address.substring(pidx + 1));

                serviceAddresses.add(new InetSocketAddress(host, port));
            }
        }

        return this;
    }

    public ClientOptions bind(String host, int port) {
        serviceAddresses.add(new InetSocketAddress(host, port));
        return this;
    }

    public List<InetSocketAddress> getServiceAddresses() {
        return serviceAddresses;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public ClientOptions addFilters(Filter... filters) {
        addFilters(Arrays.asList(filters));
        return this;
    }

    public ClientOptions addFilters(List<Filter> filters) {
        if (filters != null) {
            for (Filter filter : filters) {
                if (filter != null) {
                    this.filters.add(filter);
                }
            }
        }

        return this;
    }

    public int getConnectPoolSize() {
        return connectPoolSize;
    }

    public void setConnectPoolSize(int connectPoolSize) {
        this.connectPoolSize = connectPoolSize;
    }

}