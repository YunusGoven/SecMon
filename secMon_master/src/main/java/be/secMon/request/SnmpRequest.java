package be.secMon.request;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpRequest{
	
	public static final int DEFAULT_VERSION = SnmpConstants.version2c;
	public static final String DEFAULT_PROTOCOL = "udp";
	public static final long DEFAULT_TIMEOUT = 3 * 1000L;
	private final String id;
	private final String community;
	private final String ip;
	private final String port;
	private final String oid;
	private final int min;
	private final int max;
	private final int frequency;
	private String status;

	public SnmpRequest(String id, String community, String ip, String port, String oid,  String min, String max, String frequency) {
		this.id = id;
		this.community = community;
		this.ip = ip;
		this.port = port;
		this.oid = oid;
		this.min = Integer.parseInt(min);
		this.max = Integer.parseInt(max);
		this.frequency = Integer.parseInt(frequency);
	}
	
	public String getId() {
		return id;
	}	
	
	private CommunityTarget<Address> createDefault() {
		Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
				+ "/" + port);
		CommunityTarget<Address> target = new CommunityTarget<Address>();
		target.setCommunity(new OctetString(community));
		target.setAddress(address);
		target.setVersion(DEFAULT_VERSION);
		target.setTimeout(DEFAULT_TIMEOUT);
		return target;
	}

	public int snmpGet() {

		CommunityTarget<Address> target = createDefault();
		Snmp snmp = null;
		int val = -1;
		try {
			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oid)));

			DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			snmp.listen();
			pdu.setType(PDU.GET);
			ResponseEvent<Address> respEvent = snmp.send(pdu, target);
			PDU response = respEvent.getResponse();

			if (response != null) {
				
				for (int i = 0; i < response.size(); i++) {
					VariableBinding vb = response.get(i);
					val = vb.getVariable().toInt();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SNMP Get Exception:" + e);
		} finally {
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException ex1) {
					snmp = null;
				}
			}
		}
		return val;
	}
	
	public void checkStatus(int val) {
		if(val < 0) {
			status = "DOWN";
		} else if (val > max || val < min) {
			status = "ALARM";
		} else {
			status = "OK";
		}
	}

	public String getStatus() {
		return status;
	}
	
	public int getFrequency() {
		return frequency;
	}

}
