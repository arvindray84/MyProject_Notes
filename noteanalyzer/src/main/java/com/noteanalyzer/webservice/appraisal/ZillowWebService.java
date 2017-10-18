/**
 * 
 */
package com.noteanalyzer.webservice.appraisal;

import static com.noteanalyzer.mvc.constant.NoteConstant.DEEP_SEARCH_URL;
import static com.noteanalyzer.mvc.constant.NoteConstant.DEFAULT_DATE_FORMAT;
import static com.noteanalyzer.mvc.constant.NoteConstant.ZESTIMATE_URL;
import static com.noteanalyzer.mvc.constant.NoteConstant.ZWSID;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.noteanalyzer.appraisal.exceptions.AddressNotAvailableException;
import com.noteanalyzer.utility.NoteUtility;

/**
 * This class will fetch the value from zillow.
 * 
 * @author Arvind Ray
 *
 */
@Service("zillowWebService")
public class ZillowWebService implements IAppraisalSource {
	private static final DocumentBuilderFactory dbFac;
	private static final DocumentBuilder docBuilder;

	private final static Logger LOG = Logger.getLogger(ZillowWebService.class.getName());

	static {
		try {
			dbFac = DocumentBuilderFactory.newInstance();
			docBuilder = dbFac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private static final NumberFormat nf = NumberFormat.getCurrencyInstance();

	@Override
	public String getApprisalSourcePropertyId(String streetAddress, String cityStateZip)
			throws SAXException, IOException, AddressNotAvailableException {
		Document deepDoc = docBuilder.parse(
				DEEP_SEARCH_URL + "?zws-id=" + ZWSID + "&address=" + streetAddress + "&citystatezip=" + cityStateZip);
		Element firstResult = (Element) deepDoc.getElementsByTagName("result").item(0);
		if (firstResult == null) {
			throw new AddressNotAvailableException("Address not found with zillow in given Street Address "
					+ streetAddress + " cityStateZip " + cityStateZip);
		}
		return firstResult.getElementsByTagName("zpid").item(0).getTextContent();
	}

	@Override
	public String getMarketValueWithAddress(String streetAddress, String city, String state, String zipCode)
			throws SAXException, IOException, AddressNotAvailableException {
		String zillowId = getApprisalSourcePropertyId(streetAddress, city + " " + state + " " + zipCode);
		if (StringUtils.isEmpty(zillowId)) {
			throw new AddressNotAvailableException("Address not found with zillow in given Street Address "
					+ streetAddress + " cityStateZip " + city + " " + state + " " + zipCode);
		}

		Document valueDoc = docBuilder.parse(ZESTIMATE_URL + "?zws-id=" + ZWSID + "&zpid=" + zillowId);
		Element zestimate = (Element) valueDoc.getElementsByTagName("zestimate").item(0);
		if (zestimate == null) {
			return StringUtils.EMPTY;
		}
		Element amount = (Element) zestimate.getElementsByTagName("amount").item(0);
		String currency = amount.getAttribute("currency");
		nf.setCurrency(Currency.getInstance(currency));
		return nf.format(Double.parseDouble(amount.getTextContent()));

	}

	@Override
	public String getMarketValueWithApprisalSourceId(String appraisalSourcePropertyId)
			throws SAXException, IOException, AddressNotAvailableException {

		Document valueDoc = docBuilder.parse(ZESTIMATE_URL + "?zws-id=" + ZWSID + "&zpid=" + appraisalSourcePropertyId);
		Element zestimate = (Element) valueDoc.getElementsByTagName("zestimate").item(0);
		if (zestimate == null) {
			return StringUtils.EMPTY;
		}
		Element amount = (Element) zestimate.getElementsByTagName("amount").item(0);
		String currency = amount.getAttribute("currency");
		nf.setCurrency(Currency.getInstance(currency));
		return nf.format(Double.parseDouble(amount.getTextContent()));

	}

	@Override
	public AppraisalPropertyBean getPropertyDetailsWithAddress(String streetAddress, String city, String state,
			String zipCode) {

		LOG.info("Trying to fetch the records from Zillow web service using Address as " + streetAddress + ", " + city
				+ " ," + state + " ," + zipCode);
		AppraisalPropertyBean propertyBean = new AppraisalPropertyBean();
		propertyBean.setStreetAddress(streetAddress);
		propertyBean.setCity(city);
		propertyBean.setState(state);
		propertyBean.setZipCode(zipCode);
		Document valueDoc = null;
		try {
			valueDoc = docBuilder.parse(DEEP_SEARCH_URL + "?zws-id=" + ZWSID + "&address=" + streetAddress
					+ "&citystatezip=" + city + " " + state + " " + zipCode + "&rentzestimate=true");

			LOG.info("Data came from Zillow without parsing: " + NoteUtility.getStringFromDocument(valueDoc));
			if (valueDoc != null) {
				DateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
				NodeList nodeList = valueDoc.getElementsByTagName("zestimate");
				if (nodeList != null && nodeList.item(0) != null) {
					Element zestimate = (Element) nodeList.item(0);
					Element amount = (Element) zestimate.getElementsByTagName("amount").item(0);
					Element lastUpdatedDate = (Element) zestimate.getElementsByTagName("last-updated").item(0);
					propertyBean.setMarketValue(amount.getTextContent());
					propertyBean.setCurrency(amount.getAttribute("currency"));
					try {
						propertyBean.setLastMarketValueUpdated(df.parse(lastUpdatedDate.getTextContent()));
					} catch (DOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				nodeList = valueDoc.getElementsByTagName("homedetails");
				if (nodeList != null && nodeList.item(0) != null) {
					Element propertyDetailUrl = (Element) nodeList.item(0);
					propertyBean.setPropertyDetailUrl(propertyDetailUrl.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("graphsanddata");
				if (nodeList != null && nodeList.item(0) != null) {
					Element propertyGraphAndDataUrl = (Element) nodeList.item(0);
					propertyBean.setPropertyGraphAndDataUrl(propertyGraphAndDataUrl.getTextContent());
				}
				nodeList = valueDoc.getElementsByTagName("mapthishome");
				if (nodeList != null && nodeList.item(0) != null) {
					Element mapthishome = (Element) nodeList.item(0);
					propertyBean.setPropertyMapUrl(mapthishome.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("comparables");
				if (nodeList != null && nodeList.item(0) != null) {
					Element comparables = (Element) nodeList.item(0);
					propertyBean.setPropertyCompareUrl(comparables.getTextContent());
				}
				nodeList = valueDoc.getElementsByTagName("zpid");
				if (nodeList != null && nodeList.item(0) != null) {
					Element apprisalPropertyId = (Element) nodeList.item(0);
					propertyBean.setApprisalPropertyId(apprisalPropertyId.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("useCode");
				if (nodeList != null && nodeList.item(0) != null) {
					Element propertyType = (Element) nodeList.item(0);
					propertyBean.setPropertyType(propertyType.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("taxAssessmentYear");
				if (nodeList != null && nodeList.item(0) != null) {
					Element taxAssessmentYear = (Element) nodeList.item(0);
					propertyBean.setTaxAssessmentYear(taxAssessmentYear.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("taxAssessment");
				if (nodeList != null && nodeList.item(0) != null) {
					Element taxAssessment = (Element) nodeList.item(0);
					propertyBean.setTaxAssessmentValue(taxAssessment.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("yearBuilt");
				if (nodeList != null && nodeList.item(0) != null) {
					Element yearBuilt = (Element) nodeList.item(0);
					propertyBean.setPropertyBuiltYear(yearBuilt.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("lotSizeSqFt");
				if (nodeList != null && nodeList.item(0) != null) {
					Element lotSizeSqFt = (Element) nodeList.item(0);
					propertyBean.setPropertyLotSize(lotSizeSqFt.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("finishedSqFt");
				if (nodeList != null && nodeList.item(0) != null) {
					Element finishedSqFt = (Element) nodeList.item(0);
					propertyBean.setPropertyBuiltUpSize(finishedSqFt.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("bathrooms");
				if (nodeList != null && nodeList.item(0) != null) {
					Element bathrooms = (Element) nodeList.item(0);
					propertyBean.setNumberOfBathrooms(bathrooms.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("bedrooms");
				if (nodeList != null && nodeList.item(0) != null) {
					Element bedrooms = (Element) nodeList.item(0);
					propertyBean.setNumberOfBedrooms(bedrooms.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("lastSoldDate");
				if (nodeList != null && nodeList.item(0) != null) {
					Element lastSoldDate = (Element) nodeList.item(0);
					try {
						propertyBean.setLastSoldDate(df.parse(lastSoldDate.getTextContent()));
					} catch (DOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				nodeList = valueDoc.getElementsByTagName("lastSoldPrice");
				if (nodeList != null && nodeList.item(0) != null) {
					Element lastSoldPrice = (Element) nodeList.item(0);
					propertyBean.setLastSoldPrice(lastSoldPrice.getTextContent());
				}

				nodeList = valueDoc.getElementsByTagName("rentzestimate");
				if (nodeList != null && nodeList.item(0) != null) {
					Element rentzestimate = (Element) nodeList.item(0);
					Element amount = (Element) rentzestimate.getElementsByTagName("amount").item(0);
					propertyBean.setRentValue(amount.getTextContent());
				}
			}
		} catch (SAXException e) {
			LOG.warning("Error while parsing the Zillow webservice: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			LOG.warning("Error while fetching the Zillow webservice: " + e);
		}
		propertyBean.setAppraisalSource("Zillow");
		return propertyBean;
	}

}
