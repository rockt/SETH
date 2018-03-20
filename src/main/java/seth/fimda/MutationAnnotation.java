

   
/* Apache UIMA v3 - First created by JCasGen Tue Mar 20 15:30:56 CET 2018 */

package seth.fimda;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Mar 20 15:30:56 CET 2018
 * XML source: /home/arne/devel/Java/SETH/desc/SethTypeSystem.xml
 * @generated */
public class MutationAnnotation extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "seth.fimda.MutationAnnotation";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(MutationAnnotation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public final static String _FeatName_mtType = "mtType";
  public final static String _FeatName_wtResidue = "wtResidue";
  public final static String _FeatName_mtResidue = "mtResidue";
  public final static String _FeatName_mtPosition = "mtPosition";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_mtType = TypeSystemImpl.createCallSite(MutationAnnotation.class, "mtType");
  private final static MethodHandle _FH_mtType = _FC_mtType.dynamicInvoker();
  private final static CallSite _FC_wtResidue = TypeSystemImpl.createCallSite(MutationAnnotation.class, "wtResidue");
  private final static MethodHandle _FH_wtResidue = _FC_wtResidue.dynamicInvoker();
  private final static CallSite _FC_mtResidue = TypeSystemImpl.createCallSite(MutationAnnotation.class, "mtResidue");
  private final static MethodHandle _FH_mtResidue = _FC_mtResidue.dynamicInvoker();
  private final static CallSite _FC_mtPosition = TypeSystemImpl.createCallSite(MutationAnnotation.class, "mtPosition");
  private final static MethodHandle _FH_mtPosition = _FC_mtPosition.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected MutationAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public MutationAnnotation(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public MutationAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public MutationAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: mtType

  /** getter for mtType - gets mutation type
   * @generated
   * @return value of the feature 
   */
  public String getMtType() { return _getStringValueNc(wrapGetIntCatchException(_FH_mtType));}
    
  /** setter for mtType - sets mutation type 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMtType(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_mtType), v);
  }    
    
   
    
  //*--------------*
  //* Feature: wtResidue

  /** getter for wtResidue - gets wtResidue
   * @generated
   * @return value of the feature 
   */
  public String getWtResidue() { return _getStringValueNc(wrapGetIntCatchException(_FH_wtResidue));}
    
  /** setter for wtResidue - sets wtResidue 
   * @generated
   * @param v value to set into the feature 
   */
  public void setWtResidue(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_wtResidue), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mtResidue

  /** getter for mtResidue - gets mtResidue
   * @generated
   * @return value of the feature 
   */
  public String getMtResidue() { return _getStringValueNc(wrapGetIntCatchException(_FH_mtResidue));}
    
  /** setter for mtResidue - sets mtResidue 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMtResidue(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_mtResidue), v);
  }    
    
   
    
  //*--------------*
  //* Feature: mtPosition

  /** getter for mtPosition - gets mutation position
   * @generated
   * @return value of the feature 
   */
  public String getMtPosition() { return _getStringValueNc(wrapGetIntCatchException(_FH_mtPosition));}
    
  /** setter for mtPosition - sets mutation position 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMtPosition(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_mtPosition), v);
  }    
    
  }

    