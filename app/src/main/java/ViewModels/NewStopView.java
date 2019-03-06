package ViewModels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AA on 2017/10/1.
 */

public class NewStopView {
    public static class NewStop {
        public int seqNo;
        public String StopName;
        public int count;

        public NewStop(int seqNo, String StopName,int count) {
            this.seqNo = seqNo;
            this.StopName = StopName;
            this.count=count;
        }
    }

    public List<NewStop> NewStopList;

    public NewStopView() {
        NewStopList = new ArrayList<>();
    }
}
