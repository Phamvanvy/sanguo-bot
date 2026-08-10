import billing.*;
public class UserData implements BillingUserData
{    
    
    public UserData()
    {
    	BillingUserData.syncData[BillingUserData.syncData_length]   =""+50;
        BillingUserData.syncData[BillingUserData.syncData_count]    =""+50;
        BillingUserData.syncData[BillingUserData.syncData_tsi]      ="20100148012";
        BillingUserData.syncData[BillingUserData.syncData_msisdn]   =""+50;
        BillingUserData.syncData[BillingUserData.syncData_uid]      =""+50;
        BillingUserData.syncData[BillingUserData.syncData_opid]     =""+50;
        BillingUserData.syncData[BillingUserData.syncData_qid]      ="";
        BillingUserData.syncData[BillingUserData.syncData_qname]    ="ªü«Ë¤£¾|";
        BillingUserData.syncData[BillingUserData.syncData_wellcome] ="ÅwªïÅwªï";
        
    }
    public static String[] getTsiData()
    {
        return BillingUserData.syncData;
    }
    public static void setTsiData( String[] adata )
    {
        for(int i =0; i < adata.length; i++ )
        {
          BillingUserData.syncData[i] =   adata[i];
        }
    }
    public String getsyncData_tsi()
    {
        return BillingUserData.syncData[BillingUserData.syncData_tsi];
    }
    public void setsyncData_tsi( String value )
    {
       BillingUserData.syncData[BillingUserData.syncData_tsi] = value;
    }
    
    public String getsyncData_uid()
    {
        return BillingUserData.syncData[BillingUserData.syncData_uid];
    }
    public void setsyncData_uid( String value )
    {
       BillingUserData.syncData[BillingUserData.syncData_uid] = value;
    }
    
    public String getsyncData_opid()
    {
        return BillingUserData.syncData[BillingUserData.syncData_opid];
    }
    public void setsyncData_opid( String value )
    {
       BillingUserData.syncData[BillingUserData.syncData_opid] = value;
    }
   
}
