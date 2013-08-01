package pojo;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;
 
/**
 * 
 * Again if you want to use straight jackson you can delete all annotations
 * 
 * Party is pretty much a reference to a HashMap that contains a reference to all fields regarding that party
 * 
 *  eg:
 *    key: PARTY_NAME
 *    value: CISCO
 *   
 *    key: PARTY_ID
 *    value: 1234...
 *   
 *    etc...
 *
 */
@XmlRootElement(name="Party") 
@XmlAccessorType(XmlAccessType.FIELD)
public class Party {

	@XmlPath(".")
	@XmlJavaTypeAdapter(MapAdapter.class)
   
	HashMap <String,String> partyInfo = new HashMap<String,String>();

	public HashMap<String,String> getPartyInfo() throws Exception
	{
		return partyInfo;
	}
 }
	
/**
 * Create an entry with a hashmap<String,String> First string label, second string value
 * Create a collection of entries using the hashmap for each one
 * 
**/