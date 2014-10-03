package au.com.hotcopper;

import java.util.Date;

import quickfix.FieldNotFound;
import quickfix.field.Symbol;
import quickfix.FieldMap;
import quickfix.Group;

public class Current {
	private String code;
	private Double bid;
	private Double offer;
	private Double first;
	private Double high;
	private Double low;
	private Double last;
	private Double marketPrice;
	private Double change;
	private Double percentChange;
	private Double volume;
	private Double value;
	private Integer tradeCount;
	private Date timeLastTraded;

	public void Current() {
	}

	public String[] getCsvHeader() {
		String[] header = new String[] { "Code", "Bid", "Offer", "First",
				"High", "Low", "Last", "Market Price", "Change", "% Change",
				"Volume", "Value", "Trade Count", "Time Last Traded" };
		return header;
	}

	public String[] getCsvRow() {
		String[] dataRow = new String[] { code, bid.toString(),
				offer.toString(), first.toString(), high.toString(),
				low.toString(), last.toString(), marketPrice.toString(),
				change.toString(), percentChange.toString(), volume.toString(),
				value.toString(), tradeCount.toString(),
				timeLastTraded.toString() };
		return dataRow;
	}

	public void set(quickfix.field.Symbol value) {
		code = value.getValue();
	}

	public void set(quickfix.field.BidPx value) {
		bid = value.getValue();
	}

	public void set(quickfix.field.OfferPx value) {
		offer = value.getValue();
	}

	public void set(quickfix.field.FirstPx value) {
		first = value.getValue();
	}

	public void set(quickfix.field.HighPx value) {
		high = value.getValue();
	}

	public void set(quickfix.field.LowPx value) {
		low = value.getValue();
	}

	public void set(quickfix.field.LastPx value) {
		last = value.getValue();
	}

	public void set(quickfix.field.Price value) {
		marketPrice = value.getValue();
		change = last - first;
		percentChange = change / first;
	}

	public void set(quickfix.field.TotalVolumeTraded value) {
		volume = value.getValue();
	}

	public void set(quickfix.field.TotalNetValue netvalue) {
		value = netvalue.getValue();
	}

	public void set(quickfix.field.TotNumTradeReports value) {
		tradeCount = value.getValue();
	}

	public void set(quickfix.field.TotalVolumeTradedTime value) {
		timeLastTraded = value.getValue();
	}
}
