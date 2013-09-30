package hex;

import static org.junit.Assert.assertEquals;
import hex.glm.*;
import hex.glm.GLMParams.Family;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import water.*;
import water.deploy.Node;
import water.deploy.NodeVM;
import water.fvec.*;


public class GLMTest2  extends TestUtil {

  //------------------- simple tests on synthetic data------------------------------------

 @Test public void testGaussianRegression() throws InterruptedException, ExecutionException{
   Key raw = Key.make("gaussian_test_data_raw");
   Key parsed = Key.make("gaussian_test_data_parsed");
   Key model = Key.make("gaussian_test");
   try {
     // make data so that the expected coefficients is icept = col[0] = 1.0
     FVecTest.makeByteVec(raw, "x,y\n0,0\n1,0.1\n2,0.2\n3,0.3\n4,0.4\n5,0.5\n6,0.6\n7,0.7\n8,0.8\n9,0.9");
     Frame fr = ParseDataset2.parse(parsed, new Key[]{raw});
     new GLM2("GLM test of gaussian(linear) regression.",model,fr,false,Family.gaussian, Family.gaussian.defaultLink,0,0).fork().get();
     GLMModel m = DKV.get(model).get();
     HashMap<String, Double> coefs = m.coefficients();
     assertEquals(0.0,coefs.get("Intercept"),1e-4);
     assertEquals(0.1,coefs.get("x"),1e-4);
   }finally{
     UKV.remove(raw);
     UKV.remove(parsed);
     UKV.remove(model);
   }
 }

 /**
  * Test Poisson regression on simple and small synthetic dataset.
  * Equation is: y = exp(x+1);
  */
 @Test public void testPoissonRegression() throws InterruptedException, ExecutionException {
   Key raw = Key.make("poisson_test_data_raw");
   Key parsed = Key.make("poisson_test_data_parsed");
   Key model = Key.make("poisson_test");
   try {
     // make data so that the expected coefficients is icept = col[0] = 1.0
     FVecTest.makeByteVec(raw, "x,y\n0,2\n1,4\n2,8\n3,16\n4,32\n5,64\n6,128\n7,256");
     Frame fr = ParseDataset2.parse(parsed, new Key[]{raw});
     new GLM2("GLM test of poisson regression.",model,fr,false,Family.poisson, Family.poisson.defaultLink,0,0).fork().get();
     GLMModel m = DKV.get(model).get();
     for(double c:m.beta())assertEquals(Math.log(2),c,1e-4);
     // Test 2, example from http://www.biostat.umn.edu/~dipankar/bmtry711.11/lecture_13.pdf
     //new byte []{1,2,3,4,5,6,7,8, 9, 10,11,12,13,14},
//     new byte []{0,1,2,3,1,4,9,18,23,31,20,25,37,45});

     UKV.remove(raw);
     FVecTest.makeByteVec(raw, "x,y\n1,0\n2,1\n3,2\n4,3\n5,1\n6,4\n7,9\n8,18\n9,23\n10,31\n11,20\n12,25\n13,37\n14,45\n");
     fr = ParseDataset2.parse(parsed, new Key[]{raw});
     new GLM2("GLM test of poisson regression(2).",model,fr,false,Family.poisson, Family.poisson.defaultLink,0,0).fork().get();
     m = DKV.get(model).get();
     assertEquals(0.3396,m.beta()[1],1e-4);
     assertEquals(0.2565,m.beta()[0],1e-4);
   }finally{
     UKV.remove(raw);
     UKV.remove(parsed);
     UKV.remove(model);
   }
 }


  /**
   * Test Gamma regression on simple and small synthetic dataset.
   * Equation is: y = 1/(x+1);
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Test public void testGammaRegression() throws InterruptedException, ExecutionException {
    Key raw = Key.make("gamma_test_data_raw");
    Key parsed = Key.make("gamma_test_data_parsed");
    Key model = Key.make("gamma_test");
    try {
      // make data so that the expected coefficients is icept = col[0] = 1.0
      FVecTest.makeByteVec(raw, "x,y\n0,1\n1,0.5\n2,0.3333333\n3,0.25\n4,0.2\n5,0.1666667\n6,0.1428571\n7,0.125");
      Frame fr = ParseDataset2.parse(parsed, new Key[]{raw});
//      /public GLM2(String desc, Key dest, Frame src, Family family, Link link, double alpha, double lambda) {
      double [] vals = new double[] {1.0,1.0};
      //public GLM2(String desc, Key dest, Frame src, Family family, Link link, double alpha, double lambda) {
      new GLM2("GLM test of gamma regression.",model,fr,false,Family.gamma, Family.gamma.defaultLink,0,0).fork().get();
      GLMModel m = DKV.get(model).get();
      for(double c:m.beta())assertEquals(1.0, c,1e-4);
    }finally{
      UKV.remove(raw);
      UKV.remove(parsed);
      UKV.remove(model);
    }
  }

  //simple tweedie test
  @Test public void testTweedieRegression() throws InterruptedException, ExecutionException{
    Key raw = Key.make("gaussian_test_data_raw");
    Key parsed = Key.make("gaussian_test_data_parsed");
    Key model = Key.make("gaussian_test");
    try {
      // make data so that the expected coefficients is icept = col[0] = 1.0
      FVecTest.makeByteVec(raw, "x,y\n0,0\n1,0.1\n2,0.2\n3,0.3\n4,0.4\n5,0.5\n6,0.6\n7,0.7\n8,0.8\n9,0.9\n0,0\n1,0\n2,0\n3,0\n4,0\n5,0\n6,0\n7,0\n8,0\n9,0");
      Frame fr = ParseDataset2.parse(parsed, new Key[]{raw});
      double [] powers = new double [] {1.5,1.1,1.9};
      double [] intercepts = new double []{3.643,1.318,9.154};
      double [] xs = new double []{-0.260,-0.0284,-0.853};
      for(int i = 0; i < powers.length; ++i){
        new GLM2("GLM test of gaussian(linear) regression.",model,fr,false,Family.tweedie, Family.tweedie.defaultLink,0,0).setTweedieVarPower(powers[i]).fork().get();
        GLMModel m = DKV.get(model).get();
        HashMap<String, Double> coefs = m.coefficients();
        assertEquals(intercepts[i],coefs.get("Intercept"),1e-3);
        assertEquals(xs[i],coefs.get("x"),1e-3);
      }
    }finally{
      UKV.remove(raw);
      UKV.remove(parsed);
      UKV.remove(model);
    }
  }

  //------------ TEST on selected files form small data and compare to R results ------------------------------------
  /**
   * Simple test for poisson, gamma and gaussian families (no regularization, test both lsm solvers).
   * Basically tries to predict horse power based on other parameters of the cars in the dataset.
   * Compare against the results from standard R glm implementation.
   */
  @Test public void testCars(){
    Key parsed = Key.make("cars_parsed");
    Key model = Key.make("cars_model");
    try{
      String [] ignores = new String[]{"name"};
      String response = "power (hp)";
      Frame fr = getFrameForFile(parsed, "smalldata/cars.csv", ignores, response);
      new GLM2("GLM test on cars.",model,fr,true,Family.poisson,Family.poisson.defaultLink,0,0).run();
      GLMModel m = DKV.get(model).get();
      HashMap<String,Double> coefs = m.coefficients();
      String [] cfs1 = new String[]{"Intercept","economy (mpg)", "cylinders", "displacement (cc)", "weight (lb)", "0-60 mph (s)", "year"};
      double [] vls1 = new double []{4.9504805,-0.0095859,-0.0063046,0.0004392,0.0001762,-0.0469810,0.0002891};
      for(int i = 0; i < cfs1.length; ++i)
        assertEquals(vls1[i], coefs.get(cfs1[i]),1e-4);
      // test gamma
      double [] vls2 = new double []{8.992e-03,1.818e-04,-1.125e-04,1.505e-06,-1.284e-06,4.510e-04,-7.254e-05};
      new GLM2("GLM test on cars.",model,fr,true,Family.gamma,Family.gamma.defaultLink,0,0).run();
      m = DKV.get(model).get();
      coefs = m.coefficients();
      for(int i = 0; i < cfs1.length; ++i)
        assertEquals(vls2[i], coefs.get(cfs1[i]),1e-4);
      // test gaussian
      double [] vls3 = new double []{166.95862,-0.00531,-2.46690,0.12635,0.02159,-4.66995,-0.85724};
      new GLM2("GLM test on cars.",model,fr,true,Family.gaussian,Family.gaussian.defaultLink,0,0).run();
      m = DKV.get(model).get();
      coefs = m.coefficients();
      for(int i = 0; i < cfs1.length; ++i)
        assertEquals(vls3[i], coefs.get(cfs1[i]),1e-4);
    } finally {
      UKV.remove(parsed);
      UKV.remove(model);
    }
  }

  /**
   * Simple test for binomial family (no regularization, test both lsm solvers).
   * Runs the classical prostate, using dataset with race replaced by categoricals (probably as it's supposed to be?), in any case,
   * it gets to test correct processing of categoricals.
   *
   * Compare against the results from standard R glm implementation.
   */
  @Test public void testProstate(){
    Key parsed = Key.make("prostate_parsed");
    Key model = Key.make("prostate_model");
    File f = TestUtil.find_test_file("smalldata/glm_test/prostate_cat_replaced.csv");
    Frame fr = getFrameForFile(parsed, "smalldata/glm_test/prostate_cat_replaced.csv", new String[]{"ID"}, "CAPSULE");
    try{
      // R results
//      Coefficients:
//        (Intercept)           ID          AGE       RACER2       RACER3        DPROS        DCAPS          PSA          VOL      GLEASON
//          -8.894088     0.001588    -0.009589     0.231777    -0.459937     0.556231     0.556395     0.027854    -0.011355     1.010179
      String [] cfs1 = new String [] {"Intercept","AGE", "RACE.R2","RACE.R3", "DPROS", "DCAPS", "PSA", "VOL", "GLEASON"};
      double [] vals = new double [] {-8.14867, -0.01368, 0.32337, -0.38028, 0.55964, 0.49548, 0.02794, -0.01104, 0.97704};
      new GLM2("GLM test on prostate.",model,fr,false,Family.binomial,Family.binomial.defaultLink,0,0).run();
      GLMModel m = DKV.get(model).get();
      HashMap<String, Double> coefs = m.coefficients();
      for(int i = 0; i < cfs1.length; ++i)
        assertEquals(vals[i], coefs.get(cfs1[i]),1e-4);
      GLMValidation val = m.validation();
      assertEquals(512.3, val.nullDeviance(),1e-1);
      assertEquals(378.3, val.residualDeviance(),1e-1);
      assertEquals(396.3, val.aic(),1e-1);
    } finally {
      UKV.remove(parsed);
      UKV.remove(model);
    }
  }

  private static Frame getFrameForFile(Key outputKey, String path,String [] ignores, String response){
    File f = TestUtil.find_test_file(path);
    Key k = NFSFileVec.make(f);
    try{
      Frame fr = ParseDataset2.parse(outputKey, new Key[]{k});
      if(ignores != null)
        for(String s:ignores) UKV.remove(fr.remove(s)._key);
      // put the response to the end
      fr.add(response, fr.remove(response));
      return GLMTask.adaptFrame(fr);
    }finally{
      UKV.remove(k);
    }
  }


  public static void main(String [] args) throws Exception{
    System.out.println("Running ParserTest2");
    final int nnodes = 1;
    for( int i = 1; i < nnodes; i++ ) {
      Node n = new NodeVM(args);
      n.inheritIO();
      n.start();
    }
    H2O.waitForCloudSize(nnodes);
    System.out.println("Running...");
    new GLMTest2().testGaussianRegression();
    new GLMTest2().testPoissonRegression();
    new GLMTest2().testGammaRegression();
    new GLMTest2().testTweedieRegression();
    new GLMTest2().testProstate();
    new GLMTest2().testCars();
    System.out.println("DONE!");
  }
}
