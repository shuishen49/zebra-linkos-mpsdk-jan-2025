/*
 * CONFIDENTIAL AND PROPRIETARY
 *
 * The source code and other information contained herein is the confidential and exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 *
 * Copyright ZIH Corp. 2018
 *
 * ALL RIGHTS RESERVED
 */

package com.zebra.developerdemocard.discovery;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.sdk.common.card.printer.discovery.DiscoveredCardPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

import java.util.Iterator;
import java.util.List;

public class DiscoveredPrinterListAdapter extends ArrayAdapter {

    @LayoutRes
    private int itemLayoutId;
    private List<DiscoveredPrinter> discoveredPrinters;

    DiscoveredPrinterListAdapter(@NonNull Context context, @LayoutRes int resource, List<DiscoveredPrinter> discoveredPrinters) {
        super(context, resource);
        this.itemLayoutId = resource;
        this.discoveredPrinters = discoveredPrinters;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(itemLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.printerModel = convertView.findViewById(R.id.printerModel);
            viewHolder.printerAddress = convertView.findViewById(R.id.printerAddress);
            viewHolder.connectionTypeIcon = convertView.findViewById(R.id.connectionTypeIcon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DiscoveredPrinter printer = discoveredPrinters.get(position);
        if (printer != null) {
            String address = printer.getDiscoveryDataMap().get("ADDRESS");
            String model = printer.getDiscoveryDataMap().get("MODEL");
            viewHolder.printerAddress.setVisibility(address != null && !address.isEmpty() ? View.VISIBLE : View.GONE);
            viewHolder.printerAddress.setText(address);
            viewHolder.printerModel.setVisibility(model != null && !model.isEmpty() ? View.VISIBLE : View.GONE);
            viewHolder.printerModel.setText(model);

            if (printer instanceof DiscoveredPrinterUsb) {
                viewHolder.connectionTypeIcon.setImageResource(R.drawable.ic_usb);
            } else if (printer instanceof DiscoveredPrinterBluetooth) {
                viewHolder.connectionTypeIcon.setImageResource(R.drawable.ic_bluetooth);
            } else if (printer instanceof DiscoveredCardPrinterNetwork) {
                viewHolder.connectionTypeIcon.setImageResource(R.drawable.ic_wifi);
            }
        }

        return convertView;
    }

    public int getCount() {
        return discoveredPrinters.size();
    }

	void addPrinter(DiscoveredPrinter printer) {
		discoveredPrinters.add(printer);
		notifyDataSetChanged();
	}

	void removePrinterWithAddress(String address) {
        if (address != null) {
            Iterator<DiscoveredPrinter> i = discoveredPrinters.iterator();
            while (i.hasNext()) {
                DiscoveredPrinter printer = i.next();
                String discoveredPrinterAddress = printer.getDiscoveryDataMap().get("ADDRESS");
                if (discoveredPrinterAddress != null && discoveredPrinterAddress.equals(address)) {
                    i.remove();
                }
            }
            notifyDataSetChanged();
        }
    }
	
	DiscoveredPrinter getPrinterAt(int index) {
		return discoveredPrinters.get(index);
	}
	
	void clearPrinters() {
		discoveredPrinters.clear();
		notifyDataSetChanged();
	}

    class ViewHolder {
        ImageView connectionTypeIcon;
        TextView printerModel;
        TextView printerAddress;
    }
}