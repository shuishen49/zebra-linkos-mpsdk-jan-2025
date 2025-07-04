using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Maui.Graphics;
using Microsoft.Maui.Controls;
using Microsoft.Maui;
using Zebra.Sdk.Printer.Discovery;
using Zebra.Sdk.Comm;

namespace ConnectionMgrPartial;

public partial class ConnectionMgr
{

    public partial void GetZebraUsbDirectPrinters(DiscoveryHandler discoveryHandler)
    {
        try
        {
            foreach (DiscoveredUsbPrinter printer in UsbDiscoverer.GetZebraUsbPrinters())
            {
                discoveryHandler.FoundPrinter(printer);
            }
            discoveryHandler.DiscoveryFinished();
        }
        catch (Exception e)
        {
            discoveryHandler.DiscoveryError(e.Message);
        }
    }

    public partial List<DiscoveredPrinter> GetZebraUsbDriverPrinters()
    {
        return UsbDiscoverer.GetZebraDriverPrinters().Cast<DiscoveredPrinter>().ToList();
    }

    public partial Connection GetUsbConnection(string symbolicName)
    {
        return new UsbConnection(symbolicName);
    }

    public partial MultichannelConnection GetMultichannelBluetoothConnection(string macAddress)
    {
        return new MultichannelBluetoothConnection(macAddress);
    }

    public partial StatusConnection GetBluetoothStatusConnection(string macAddress)
    {
        return new BluetoothStatusConnection(macAddress);
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
        BluetoothConnection connection = new BluetoothConnection(macAddress);
        connection.Open(); // Check connection

        try
        {
            ServiceDiscoveryHandlerImplementation serviceDiscoveryHandler = new ServiceDiscoveryHandlerImplementation();
            BluetoothDiscoverer.FindServices(macAddress, serviceDiscoveryHandler);

            while (!serviceDiscoveryHandler.Finished)
            {
                Task.Delay(100);
            }

            StringBuilder sb = new StringBuilder();
            foreach (ConnectionChannel connectionChannel in serviceDiscoveryHandler.ConnectionChannels)
            {
                sb.AppendLine(connectionChannel.ToString());
            }
            return sb.ToString();
        }
        finally
        {
            try
            {
                connection?.Close();
            }
            catch (ConnectionException) { }
        }
    }


    private class ServiceDiscoveryHandlerImplementation : ServiceDiscoveryHandler
    {

        public List<ConnectionChannel> ConnectionChannels { get; private set; }

        public bool Finished { get; private set; }

        public ServiceDiscoveryHandlerImplementation()
        {
            ConnectionChannels = new List<ConnectionChannel>();
        }

        public void DiscoveryFinished()
        {
            Finished = true;
        }

        public void FoundService(ConnectionChannel channel)
        {
            ConnectionChannels.Add(channel);
        }
    }
}

