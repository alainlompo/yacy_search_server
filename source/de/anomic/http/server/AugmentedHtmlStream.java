package de.anomic.http.server;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import net.yacy.cora.protocol.RequestHeader;
import net.yacy.interaction.AugmentHtmlStream;
import net.yacy.kelondro.data.meta.DigestURI;
import net.yacy.search.Switchboard;

public class AugmentedHtmlStream extends FilterOutputStream {
	private final Writer out;
	private final ByteArrayOutputStream buffer;
	private final Charset charset;
	private final DigestURI url;
	private final RequestHeader requestHeader;

	public AugmentedHtmlStream(OutputStream out, Charset charset, DigestURI url, RequestHeader requestHeader) {
		super(out);
		this.out = new BufferedWriter(new OutputStreamWriter(out, charset));
		this.buffer = new ByteArrayOutputStream();
		this.charset = charset;
		this.url = url;
		this.requestHeader = requestHeader;
	}

	@Override
    public void write(int b) throws IOException {
		this.buffer.write(b);
	}

	@Override
    public void write(byte[] b, int off, int len) throws IOException {
		this.buffer.write(b, off, len);
	}

	@Override
    public void close() throws IOException {
		StringBuffer b = new StringBuffer(this.buffer.toString(this.charset.name()));
		b = process(b);
		this.out.write(b.toString());
		this.out.close();
	}

	public StringBuffer process(StringBuffer data) {

		if (Switchboard.getSwitchboard().getConfigBool("proxyAugmentation", false) == true) {

			if (!this.url.toNormalform(false, true).contains("currentyacypeer/")) {

				return AugmentHtmlStream.process (data, this.charset, this.url, this.requestHeader);

			} else {
				return data;
			}

		} else {
			return data;
		}
	}

	public static boolean supportsMime(String mime) {
//		System.out.println("mime" +mime);
		return mime.split(";")[0].equals("text/html");
	}

}