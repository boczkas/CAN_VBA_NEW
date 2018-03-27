public class Message{
    private String messageID;
    private String messageData;
    private String messageCheckSum;

    // do konstruktora pozniej dodac wszystkie pola jak juz bede mial je powyliczane

    public Message(String messageID) {
        this.messageID = messageID;
        this.messageData = "";
        this.messageCheckSum = "";
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessageData() {
        return messageData;
    }

    public String getMessageCheckSum() {
        return messageCheckSum;
    }
}
