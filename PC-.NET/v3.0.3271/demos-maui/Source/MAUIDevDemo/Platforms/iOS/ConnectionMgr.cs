using System;
using System.Collections.Generic;
using Zebra.Sdk.Comm;
using Zebra.Sdk.Printer.Discovery;
using Microsoft.Maui.Controls;
using Microsoft.Maui;
using MauiDevDemo;

namespace ConnectionMgrPartial;

public partial class ConnectionMgr
{

    public partial void GetZebraUsbDirectPrinters(DiscoveryHandler discoveryHandler)
    {
        throw new NotImplementedException();
    }

    public partial List<DiscoveredPrinter> GetZebraUsbDriverPrinters()
    {
        throw new NotImplementedException();
    }

    public partial Connection GetUsbConnection(string symbolicName)
    {
        throw new NotImplementedException();
    }

    public partial MultichannelConnection GetMultichannelBluetoothConnection(string macAddress)
    {
        throw new NotImplementedException();
    }

    public partial StatusConnection GetBluetoothStatusConnection(string macAddress)
    {
        throw new NotImplementedException();
    }

    public partial Connection GetBluetoothConnection(string macAddress)
    {
        return new BluetoothConnection(macAddress);
    }

    public partial void FindBluetoothPrinters(DiscoveryHandler discoveryHandler)
    {
        BluetoothDiscoverer.FindPrinters(discoveryHandler);
    }

    public partial string BuildBluetoothConnectionChannelsString(string macAddress)
    {
        throw new NotImplementedException();
    }
}

