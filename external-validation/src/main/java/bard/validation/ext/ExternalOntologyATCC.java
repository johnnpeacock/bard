package bard.validation.ext;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.scripps.fl.entrez.internal.HttpClientBaseSingleton;

public class ExternalOntologyATCC extends ExternalOntologyAPI {
	
	public static class ATCCCreator implements ExternalOntologyCreator {
		@Override
		public ExternalOntologyAPI create(URI uri, Properties props) throws ExternalOntologyException {
			String host = uri.getHost();
			if (host.endsWith(".atcc.org"))
				return new ExternalOntologyATCC();
			return null;
		}
	}

	/**
	 * Designed to be run against http://www.atcc.org/Search_Results.aspx. Look
	 * for anchors where the href is for the single product page. The contents
	 * of the anchor should be the display value. Avoids duplicates where the
	 * contents of the tag equal the Id.
	 */
	class ATCCMultiProductHandler extends DefaultHandler {
		StringBuilder displayTag;
		Set<String> ids = new HashSet<String>();
		ExternalItem item;
		List<ExternalItem> items = new ArrayList<ExternalItem>();
		Pattern pattern = Pattern.compile("/Products/All/(.*)\\.aspx$");

		public void characters(char[] ch, int start, int length) {
			if (displayTag != null)
				displayTag.append(new String(ch, start, length).trim());
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("a".equalsIgnoreCase(qName) && displayTag != null) {
				if (!ids.contains(item.getId())) {
					ids.add(item.getId());
					item.setDisplay(displayTag.toString().trim());
					items.add(item);
				}
				displayTag = null;
			}
		}

		public List<ExternalItem> getItems() {
			return items;
		}

		public void startElement(String uri, String localName, String name, Attributes attributes) {
			if ("a".equalsIgnoreCase(name)) {
				String href = attributes.getValue("href");
				href = href == null ? "" : href;
				Matcher matcher = pattern.matcher(href);
				if (matcher.find()) {
					String id = matcher.group(1);
					item = new ExternalItem(id, "");
					displayTag = new StringBuilder();
				}
			}
		}
	}

	/**
	 * Designed to be run against the http://www.atcc.org/Products/All/ page. If
	 * the <title/> returned is "Page Not Found" then it isn't a valid ATTC
	 * product, otherwise it is the product Id.
	 * 
	 * The contents of the //h1[class="product_name"] element should then be the
	 * description
	 */
	class ATCCSingleProductHandler extends DefaultHandler {
		String display;
		String id;
		boolean notFound = false;
		StringBuilder productTag;
		StringBuilder titleTag;

		public void characters(char[] ch, int start, int length) {
			if (titleTag != null)
				titleTag.append(new String(ch, start, length).trim());
			if (productTag != null)
				productTag.append(new String(ch, start, length).trim());
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("title".equalsIgnoreCase(qName)) {
				if ("Page Not Found".equals(titleTag.toString())) {
					notFound = true;
					throw new SAXException(new BreakParsingException());
				} else {
					id = titleTag.toString();
					titleTag = null;
				}
			} else if ("h1".equalsIgnoreCase(qName) && productTag != null) {
				display = productTag.toString();
				productTag = null;
				throw new SAXException(new BreakParsingException());
			}
		}

		public String getDisplay() {
			return display;
		}

		public String getId() {
			return id;
		}

		public boolean isNotFound() {
			return notFound;
		}

		public void startElement(String uri, String localName, String name, Attributes a) {
			if ("title".equalsIgnoreCase(name))
				titleTag = new StringBuilder();
			if ("h1".equalsIgnoreCase(name) && "product_name".equalsIgnoreCase(a.getValue("class")))
				productTag = new StringBuilder();
		}
	}

	class BreakParsingException extends RuntimeException {
	}

	private static String ATCC_ITEM_URL_FORMAT = "http://www.atcc.org/Products/All/%s";

	private static String ATCC_SEARCH_URL_FORMAT = "http://www.atcc.org/Search_Results.aspx?dsNav=Rpp:%s&searchTerms=%s";

	private static boolean DEBUGGING = false;

	private static Pattern ID_PATTERN = Pattern.compile("^.*ATCC�?\\s*([\\w\\d-]+)�?\\)?$"); // "^(ATCC\\s*)(.+)$"

	private static final Logger log = LoggerFactory.getLogger(ExternalOntologyATCC.class);

	@Override
	public String cleanId(String id) {
		id = super.cleanId(id);
		Matcher matcher = ID_PATTERN.matcher(id);
		if (matcher.matches())
			id = matcher.group(1);
		return id;
	}

	@Override
	public String cleanName(String name) {
		return cleanId(name);
	}

	@Override
	public ExternalItem findById(String id) throws ExternalOntologyException {
		id = cleanId(id);
		try {
			URL url = new URL(String.format(ATCC_ITEM_URL_FORMAT, id));
			ATCCSingleProductHandler handler = new ATCCSingleProductHandler();
			handle(url, handler);
			if (handler.isNotFound())
				return null;
			else
				return new ExternalItem(handler.getId(), handler.getDisplay());
		} catch (Exception ex) {
			throw new ExternalOntologyException(ex);
		}
	}

	@Override
	public ExternalItem findByName(String name) throws ExternalOntologyException {
		return findById(name);
	}

	/**
	 * Hard limit of 100 records - scraping a web page, obeying its contract.
	 */
	@Override
	public List<ExternalItem> findMatching(String term, int limit) throws ExternalOntologyException {
		try {
			if(limit <= 0 || limit > 100) 
				limit = 100;
			URL url = new URL(String.format(ATCC_SEARCH_URL_FORMAT, limit, queryGenerator(term)));
			ATCCMultiProductHandler handler = new ATCCMultiProductHandler();
			handle(url, handler);
			return handler.getItems();
		} catch (Exception ex) {
			throw new ExternalOntologyException(ex);
		}
	}

	@Override
	public String getExternalURL(String id) {
		return String.format(ATCC_ITEM_URL_FORMAT, cleanId(id));
	}

	protected DefaultHandler handle(URL url, DefaultHandler handler) throws IOException, SAXException, ParserConfigurationException {
		BufferedInputStream is = null;
		try {
			log.debug(String.format("GET %s", url));
			HttpGet get = new HttpGet(url.toString());
			// get allows redirects by default, post does not. USER-AGENT seems
			// to make it run faster
			get.setHeader("USER-AGENT",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
			HttpResponse response = HttpClientBaseSingleton.getInstance().getHttpClient().execute(get);
			is = new BufferedInputStream(response.getEntity().getContent());
			if (DEBUGGING) {
				File file = File.createTempFile(getClass().getName(), ".html");
				log.debug("Copying stream to " + file);
				FileOutputStream fos = new FileOutputStream(file);
				IOUtils.copy(is, fos);
				fos.close();
				is.close();
				Desktop.getDesktop().open(file);
				is = new BufferedInputStream(new FileInputStream(file));
			}
			SAXParserImpl.newInstance(null).parse(is, handler);
		} catch (SAXException ex) {
			if (!(ex.getCause() instanceof BreakParsingException))
				throw ex;
		} finally {
			IOUtils.closeQuietly(is);
		}
		return handler;
	}

	@Override
	public String queryGenerator(String term) {
		return cleanId(term);
	}
}