using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Zebra.Sdk.Printer.Discovery;
using Zebra.Sdk.Comm;

namespace ConnectionMgrPartial
{
    public partial class ConnectionMgr
    {
        public partial string BuildBluetoothConnectionChannelsString(string macAddress);

        public partial void FindBluetoothPrinters(DiscoveryHandler discoveryHandler);

        public partial Connection GetBluetoothConnection(string macAddress);

        public partial StatusConnection GetBluetoothStatusConnection(string macAddress);

        public partial MultichannelConnection GetMultichannelBluetoothConnection(string macAddress);

        public partial Connection GetUsbConnection(string symbolicName);

        public partial void GetZebraUsbDirectPrinters(DiscoveryHandler discoveryHandler);

        public partial List<DiscoveredPrinter> GetZebraUsbDriverPrinters();
    }
}
