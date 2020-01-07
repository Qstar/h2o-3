package hex.gam.MatrixFrameUtils;

import hex.gam.GAMModel.GAMParameters;
import hex.glm.GLMModel;
import water.MemoryManager;
import water.fvec.Frame;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class GamUtils {
  /***
   * Allocate 3D array to store various info.
   * @param num2DArrays
   * @param parms
   * @param fileMode: 0: allocate for transpose(Z), 1: allocate for S, 2: allocate for t(Z)*S*Z
   * @return
   */
  public static double[][][] allocate3DArray(int num2DArrays, GAMParameters parms, int fileMode) {
    double[][][] array3D = new double[num2DArrays][][];
    for (int frameIdx = 0; frameIdx < num2DArrays; frameIdx++) {
      int numKnots = parms._k[frameIdx];
      switch (fileMode) {
        case 0: array3D[frameIdx] = MemoryManager.malloc8d(numKnots-1, numKnots); break;
        case 1: array3D[frameIdx] = MemoryManager.malloc8d(numKnots, numKnots); break;
        case 2: array3D[frameIdx] = MemoryManager.malloc8d(numKnots-1, numKnots-1); break;
        default: throw new IllegalArgumentException("fileMode can only be 0, 1 or 2.");
      }
    }
    return array3D;
  }

  public static void copy2DArray(double[][] src_array, double[][] dest_array) {
    int numRows = src_array.length;
    for (int colIdx = 0; colIdx < numRows; colIdx++) { // save zMatrix for debugging purposes or later scoring on training dataset
      System.arraycopy(src_array[colIdx], 0, dest_array[colIdx], 0,
              src_array[colIdx].length);
    }
  }

  public static GLMModel.GLMParameters copyGAMParams2GLMParams(GAMParameters parms, Frame trainData) {
    GLMModel.GLMParameters glmParam = new GLMModel.GLMParameters();
    Field[] gamFields = GAMParameters.class.getDeclaredFields();
    // assign relevant GAMParameter fields to GLMParameter fields
    List<String> gamOnlyList = Arrays.asList(new String[]{"_k", "_gam_X", "_bs", "_scale", "_train",
            "_ignored_columns", "_saveZMatrix", "_saveGamCols", "_savePenaltyMat"});
    for (Field oneField : gamFields) {
      try {
        if (!gamOnlyList.contains(oneField.getName())) {
          Field glmField = glmParam.getClass().getDeclaredField(oneField.getName());
          glmField.set(glmParam, oneField.get(parms));
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
    }
    glmParam._train = trainData._key;
    return glmParam;
  }
}
