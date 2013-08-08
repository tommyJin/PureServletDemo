package pojo;

import java.io.Serializable;
import java.util.HashMap;
 
public class PartyPojo implements Serializable {

	private static final long serialVersionUID = -4892745279086305480L;
   
	HashMap <String,String> partyInfo = new HashMap<String,String>();

	public HashMap<String,String> getPartyInfo() throws Exception
	{
		return partyInfo;
	}
 }