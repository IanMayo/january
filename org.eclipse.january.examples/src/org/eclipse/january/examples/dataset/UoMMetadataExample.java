package org.eclipse.january.examples.dataset;

import java.util.Arrays;

import org.eclipse.january.dataset.CompoundDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.LongDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.internal.AxesMetadataImpl;
import org.eclipse.uomo.units.SI;
import org.junit.Before;
import org.junit.Test;
import org.unitsofmeasurement.unit.Unit;

public class UoMMetadataExample
{

  @Before
  public void before() {
    // prevent missing logger from being reported
    Utils.suppressSLF4JError();
  }

  /** experiment with extending maths processing to be AxesMetadata aware
   * 
   * @author ian
   *
   */
  private static class NewMaths extends Maths
  {
    public static Dataset multiply(final Object a, final Object b,
        final Dataset o)
    {
      // TODO: check for compliant units?
      Dataset res = Maths.multiply(a, b, o);

      // TODO: if there are indices, we need to check they of the same
      // type, and to do some interpolation if they aren't synced

      // do the necessary metadata type manipulation for the output set
      if (a instanceof Dataset && b instanceof Dataset)
      {
        Dataset aD = (Dataset) a;
        Dataset bD = (Dataset) b;
        UomMetadata aMetadata = aD.getFirstMetadata(UomMetadata.class);
        UomMetadata bMetadata = bD.getFirstMetadata(UomMetadata.class);
        Unit<?> outUnit = aMetadata.getUnit().multiply(bMetadata.getUnit());
        res.addMetadata(new UomMetadata(outUnit));
      }

      return res;
    }

    public static Dataset add(final Object a, final Object b, final Dataset o)
    {
      // TODO: check for compliant units?
      Dataset res = Maths.add(a, b, o);

      // do the necessary metadata type manipulation for the output set
      if (a instanceof Dataset && b instanceof Dataset)
      {
        Dataset aD = (Dataset) a;
        UomMetadata aMetadata = aD.getFirstMetadata(UomMetadata.class);
        Unit<?> outUnit = aMetadata.getUnit();
        res.addMetadata(new UomMetadata(outUnit));
      }
      return res;
    }

    public static Dataset subtract(final Object a, final Object b,
        final Dataset o)
    {
      // TODO: check for compliant units?
      Dataset res = Maths.subtract(a, b, o);

      // TODO: if there are indices, we need to check they of the same
      // type, and to do some interpolation if they aren't synced

      // do the necessary metadata type manipulation for the output set
      if (a instanceof Dataset && b instanceof Dataset)
      {
        Dataset aD = (Dataset) a;
        UomMetadata aMetadata = aD.getFirstMetadata(UomMetadata.class);
        Unit<?> outUnit = aMetadata.getUnit();
        res.addMetadata(new UomMetadata(outUnit));
      }
      return res;
    }

    public static Dataset divide(final Object a, final Object b,
        final Dataset o)
    {
      Dataset res = Maths.divide(a, b, o);

      // do the necessary metadata type manipulation for the output set
      if (a instanceof Dataset && b instanceof Dataset)
      {
        Dataset aD = (Dataset) a;
        Dataset bD = (Dataset) b;
        UomMetadata aMetadata = aD.getFirstMetadata(UomMetadata.class);
        UomMetadata bMetadata = bD.getFirstMetadata(UomMetadata.class);
        Unit<?> outUnit = aMetadata.getUnit().divide(bMetadata.getUnit());
        res.addMetadata(new UomMetadata(outUnit));
      }

      return res;
    }
  }


  @Test
  public void testUnitsAwareOperations()
  {
    Dataset timestampsA = DatasetFactory.createFromList(Arrays.asList(100l,
        200l, 300l));
    timestampsA.addMetadata(new UomMetadata(SI.SECOND));

    AxesMetadata axesMetadataA = new AxesMetadataImpl();
    axesMetadataA.initialize(1);
    axesMetadataA.setAxis(0, timestampsA);

    Dataset elapsedTime = DatasetFactory.createFromList(Arrays.asList(100l,
        200l, 300l));
    elapsedTime.addMetadata(axesMetadataA);
    elapsedTime.addMetadata(new UomMetadata(SI.SECOND));
    elapsedTime.setName("ElapsedTime");

    Dataset speed = DatasetFactory.createFromList(Arrays.asList(1d, 2d, 3d));
    speed.addMetadata(axesMetadataA);
    speed.addMetadata(new UomMetadata(SI.METRES_PER_SECOND));
    speed.setName("Speed");

    header("Unit-aware operations");

    printTimedDataset(elapsedTime);
    printTimedDataset(speed);
    printTimedDataset(NewMaths.multiply(speed, elapsedTime, null));
    printTimedDataset(NewMaths.divide(speed, elapsedTime, null));
    printTimedDataset(NewMaths.add(speed, elapsedTime, null));
    printTimedDataset(NewMaths.subtract(speed, elapsedTime, null));
  }

  public void header(String title)
  {
    System.out.println("=== " + title + " ===");
  }

  public void printTimedDataset(Dataset dataset)
  {
    printTimedDataset(dataset, dataset.getFirstMetadata(AxesMetadata.class));
  }

  public void printTimedDataset(Dataset dataset, AxesMetadata axesMetadata)
  {
    IndexIterator iterator = dataset.getIterator();
    UomMetadata uomMetadata = dataset.getFirstMetadata(UomMetadata.class);
    String unit = uomMetadata != null ? uomMetadata.getUnit().toString() : "";

    final String axisUnit;
    final LongDataset axisDataset;
    if (axesMetadata != null && axesMetadata.getAxes().length > 0)
    {
      axisDataset = (LongDataset) axesMetadata.getAxes()[0];
      UomMetadata axusUomMetadata = axisDataset.getFirstMetadata(
          UomMetadata.class);
      axisUnit = axusUomMetadata != null ? axusUomMetadata.getUnit().toString()
          : "";
    }
    else
    {
      axisUnit = "N/A";
      axisDataset = null;
    }

    System.out.println(dataset.getName() + ":");
    while (iterator.hasNext())
    {
      final String indexVal;
      if (axisDataset != null)
      {
        indexVal = axisDataset.getLong(iterator.index) + "[" + axisUnit + "]";
      }
      else
      {
        indexVal = "N/A";
      }

      System.out.print(indexVal + " : " + dataset.getDouble(iterator.index)
          + "[" + unit + "]");
      System.out.print("; ");
    }
    System.out.println();
  }

  public void printTimedCompoundDataset(CompoundDataset compoundDataset)
  {
    AxesMetadata axesMetadata = compoundDataset.getFirstMetadata(
        AxesMetadata.class);
    int size = compoundDataset.getElementsPerItem();
    System.out.println(compoundDataset.getName() + "{");
    for (int i = 0; i < size; i++)
    {
      System.out.print("\t");
      printTimedDataset(compoundDataset.getElements(i), axesMetadata);
    }
    System.out.println("}");
  }
}