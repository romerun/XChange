package com.xeiam.xchange.streaming.websocket;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.xeiam.xchange.streaming.websocket.WebSocket.Role;
import com.xeiam.xchange.streaming.websocket.exceptions.InvalidDataException;
import com.xeiam.xchange.streaming.websocket.exceptions.InvalidHandshakeException;
import com.xeiam.xchange.streaming.websocket.exceptions.LimitExceededException;
import com.xeiam.xchange.utils.CharsetUtils;

public abstract class Draft {

  public enum HandshakeState {
    /**
     * Handshake matched this Draft successfully
     */
    MATCHED,
    /**
     * Handshake is does not match this Draft
     */
    NOT_MATCHED,
    /**
     * Handshake matches this Draft but is not complete
     */
    MATCHING
    // ,/**Can not yet say anything*/
    // PENDING not yet in use
  }

  private static final byte[] FLASH_POLICY_REQUEST = CharsetUtils.toByteArrayUtf8("<policy-file-request/>");

  protected Role role = null;

  public static ByteBuffer readLine(ByteBuffer buf) {

    ByteBuffer sbuf = ByteBuffer.allocate(buf.remaining());
    byte prev = '0';
    byte cur = '0';
    while (buf.hasRemaining()) {
      prev = cur;
      cur = buf.get();
      sbuf.put(cur);
      if (prev == (byte) '\r' && cur == (byte) '\n') {
        sbuf.limit(sbuf.position() - 2);
        sbuf.position(0);
        return sbuf;

      }
    }
    // ensure that there wont be any bytes skipped
    buf.position(buf.position() - sbuf.position());
    return null;
  }

  public static String readStringLine(ByteBuffer buf) {

    ByteBuffer b = readLine(buf);
    return b == null ? null : CharsetUtils.toStringAscii(b.array(), 0, b.limit());
  }

  public static HandshakeBuilder translateHandshakeHttp(ByteBuffer buf) throws InvalidHandshakeException {

    DefaultHandshakeData draft = new DefaultHandshakeData();

    String line = readStringLine(buf);
    if (line == null) {
      throw new InvalidHandshakeException("could not match http status line");
    }

    String[] firstLineTokens = line.split(" ");// eg. GET / HTTP/1.1
    if (firstLineTokens.length < 3) {
      throw new InvalidHandshakeException("could not match http status line");
    }
    draft.setResourceDescriptor(firstLineTokens[1]);

    line = readStringLine(buf);
    while (line != null && line.trim().length() > 0) {
      String[] pair = line.split(":", 2);
      if (pair.length != 2) {
        throw new InvalidHandshakeException("not an http header");
      }
      draft.put(pair[0], pair[1].replaceFirst("^ +", ""));
      line = readStringLine(buf);
    }
    return draft;
  }

  public abstract HandshakeState acceptHandshakeAsClient(HandshakeData request, HandshakeData response) throws InvalidHandshakeException;

  public abstract HandshakeState acceptHandshakeAsServer(HandshakeData handshakeData) throws InvalidHandshakeException;

  protected boolean basicAccept(HandshakeData handshakeData) {

    return handshakeData.getFieldValue("Upgrade").equalsIgnoreCase("websocket") && handshakeData.getFieldValue("Connection").toLowerCase(Locale.ENGLISH).contains("upgrade");
  }

  public abstract ByteBuffer createBinaryFrame(FrameData frameData);

  public abstract List<FrameData> createFrames(byte[] binary, boolean mask);

  public abstract List<FrameData> createFrames(String text, boolean mask);

  public abstract void reset();

  public List<ByteBuffer> createHandshake(HandshakeData handshakeData, Role ownrole) {

    return createHandshake(handshakeData, ownrole, true);
  }

  public List<ByteBuffer> createHandshake(HandshakeData handshakeData, Role ownrole, boolean withcontent) {

    StringBuilder bui = new StringBuilder(100);
    if (ownrole == Role.CLIENT) {
      bui.append("GET ");
      bui.append(handshakeData.getResourceDescriptor());
      bui.append(" HTTP/1.1");
    } else if (ownrole == Role.SERVER) {
      bui.append("HTTP/1.1 101 " + handshakeData.getHttpStatusMessage());
    } else {
      throw new RuntimeException("unknow role");
    }
    bui.append("\r\n");
    Iterator<String> it = handshakeData.iterateHttpFields();
    while (it.hasNext()) {
      String fieldname = it.next();
      String fieldvalue = handshakeData.getFieldValue(fieldname);
      bui.append(fieldname);
      bui.append(": ");
      bui.append(fieldvalue);
      bui.append("\r\n");
    }
    bui.append("\r\n");
    byte[] httpheader = CharsetUtils.toByteArrayAscii(bui.toString());

    byte[] content = withcontent ? handshakeData.getContent() : null;
    ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0 : content.length) + httpheader.length);
    bytebuffer.put(httpheader);
    if (content != null) {
      bytebuffer.put(content);
    }
    bytebuffer.flip();
    return Collections.singletonList(bytebuffer);
  }

  public abstract HandshakeBuilder postProcessHandshakeRequestAsClient(HandshakeBuilder request) throws InvalidHandshakeException;

  public abstract HandshakeBuilder postProcessHandshakeResponseAsServer(HandshakeData request, HandshakeBuilder response) throws InvalidHandshakeException;

  public abstract List<FrameData> translateFrame(ByteBuffer buffer) throws InvalidDataException;

  public HandshakeData translateHandshake(ByteBuffer buf) throws InvalidHandshakeException {

    return translateHandshakeHttp(buf);
  }

  public int checkAlloc(int bytecount) throws LimitExceededException, InvalidDataException {

    if (bytecount < 0) {
      throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR, "Negative count");
    }
    return bytecount;
  }

  public void setParseMode(Role role) {

    this.role = role;
  }

}