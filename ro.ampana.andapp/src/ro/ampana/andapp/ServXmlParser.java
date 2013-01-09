package ro.ampana.andapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class ServXmlParser {
	// We don't use namespaces
	private static final String ns = null;

	public List parse(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}

	private List readFeed(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		List entries = new ArrayList();
		parser.require(XmlPullParser.START_TAG, ns, "markers");
		int et = parser.next();
		
//		parser.require(XmlPullParser.START_TAG, ns, "marker");		
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			
//			et = parser.next();
//			while (parser.next() != XmlPullParser.END_TAG) {
//				 if (parser.getEventType() != XmlPullParser.START_TAG) {
//				 continue;
//				 }

				String name = parser.getName();

				// Starts by looking for the entry tag
				if (name.equals("marker")) {
					entries.add(readEntry(parser));
				} else {
					skip(parser);
				}
//				et = parser.
//			}
			// parser.nextTag();
		}

		// et = parser.getEventType();

		// }
		return entries;
	}

	public static class Entry {
		public final Long wpid;
		public final String name;
		public final String desc;

		private Entry(Long wpid, String name, String desc) {
			this.wpid = wpid;
			this.name = name;
			this.desc = desc;
		}
	}

	// Parses the contents of an entry.
	private Entry readEntry(XmlPullParser parser)
			throws XmlPullParserException, IOException {

		String name = parser.getAttributeValue(null, "name");// null;
		String desc = parser.getAttributeValue(null, "description");// null;
		Long wpid = Long.decode(parser.getAttributeValue(null, "wpid"));// null;
		return new Entry(wpid, name, desc);
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

}