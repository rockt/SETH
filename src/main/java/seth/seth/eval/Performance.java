package seth.seth.eval;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 17.01.13
 * Time: 15:06
 * To change this template use File | Settings | File Templates.
 *
 * Class is used to calculate NER performance on different  corpora
 */
class Performance {

        protected int tp;
        protected int tn;
        protected int fp;
        protected int fn;

        private double precision;
        private double recall;
        private double f1;


        public void addTP(){
            tp++;
        }

        public void addTP(int count){
            tp+=count;
        }

        public void addTN(){
            tn++;
        }
    public void addTN(int count){
        tn+=count;
    }

        public void addFP(){
            fp++;
        }
    public void addFP(int count){
        fp+=count;
    }

        public void addFN(){
            fn++;
        }
    public void addFN(int count){
        fn+=count;
    }

        public int getTP() {
            return tp;
        }
        public int getTN() {
            return tn;
        }
        public int getFP() {
            return fp;
        }
        public int getFN() {
            return fn;
        }
        public double getPrecision() {
            return precision;
        }
        public double getRecall() {
            return recall;
        }
        public double getF1() {
            return f1;
        }
        public int getAll(){
            return tp + tn + fp + fn;
        }

        /**
         * Calculates precision recall and so on
         */
        public void calculate(){
            precision =  tp / ((float)(tp+fp));
            recall = tp / ((float)(tp+fn));
            f1 = 2*(precision*recall)/(precision+recall);
        }
}
