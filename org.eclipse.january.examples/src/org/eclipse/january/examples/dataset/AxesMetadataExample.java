package org.eclipse.january.examples.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.LongDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.internal.AxesMetadataImpl;
import org.junit.Before;
import org.junit.Test;

public class AxesMetadataExample
{

  /**
   * experiment with extending maths processing to be AxesMetadata aware
   * 
   * @author ian
   *
   */
  private static class NewMaths extends Maths
  {
    public static Dataset add(final Object a, final Object b, final Dataset o)
    {
      // perform the basic operation
      final Dataset res = Maths.add(a, b, o);

      // ok, we'll use this as the target metadata
      if (a instanceof IDataset)
      {
        final IDataset ds = (IDataset) a;
        final AxesMetadata targetAxes = ds.getFirstMetadata(AxesMetadata.class);
        if (targetAxes != null)
        {
          res.addMetadata(targetAxes);
        }
      }

      return res;
    }

    public static Dataset add2(final Object a, final Object b, final Dataset o)
    {
      final Object operandA;
      final Object operandB;

      // ok, check if we've received datasets
      if (a instanceof Dataset && b instanceof Dataset)
      {
        final Dataset da = (Dataset) a;
        final Dataset db = (Dataset) b;
        final AxesMetadata axesA = da.getFirstMetadata(AxesMetadata.class);
        final AxesMetadata axesB = db.getFirstMetadata(AxesMetadata.class);

        if (axesA != null && axesB != null)
        {
          // ok, we've got indexed data. see if they match
          ILazyDataset aLazyIndices = axesA.getAxis(0)[0];
          ILazyDataset bLazyIndices = axesB.getAxis(0)[0];

          // ok, if they're different length then we know they're non-synced
          final boolean needInterp;
          if (aLazyIndices.getSize() != bLazyIndices.getSize())
          {
            // ok, need syncing
            needInterp = true;
          }
          else if (aLazyIndices.equals(bLazyIndices))
          {
            // ok, they're equal, no need to interp
            needInterp = false;
          }
          else
          {
            // values don't match, need to interp
            needInterp = true;
          }

          if (needInterp)
          {
            // ok, see which dataset has the wider range
            // first we need to load the data
            Dataset aIndices = null;
            Dataset bIndices = null;
            boolean success = false;
            try
            {
              aIndices = DatasetUtils.sliceAndConvertLazyDataset(
                  aLazyIndices);
              bIndices = DatasetUtils.sliceAndConvertLazyDataset(
                  bLazyIndices);
              success = true;
            }
            catch (DatasetException e)
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            
            if(success)
            {
              final double aMin = aIndices.min(false).doubleValue();
              final double aMax = aIndices.max(false).doubleValue();

              final double bMin = bIndices.min(false).doubleValue();
              final double bMax = bIndices.max(false).doubleValue();

              // do we use "a" as the time-master?
              final boolean useA;

              if (aMax < bMin || aMin > bMax)
              {
                // keep compiler happy
                operandA = null;
                operandB = null;

                // datasets don't overlap
                throw new IllegalArgumentException(
                    "The indices of the dataset do not overlap");
              }
              else if (aMin <= bMin && aMax >= bMax)
              {
                // ok, use a, it contains b
                useA = false;
              }
              else if (bMin < aMin && bMax > aMax)
              {
                // ok, use b, it contains a
                useA = true;
              }
              else if (aIndices.getSize() > bIndices.getSize())
              {
                // ok, one isn't contained within the other. Sort out which to use
                // initially, take the longer dataset. In the future use the one
                // with the most measurements in the overlapping period

                // ok, use a
                useA = true;
              }
              else
              {
                // ok, use b
                useA = false;
              }

              final Dataset indices = useA ? bIndices : aIndices;
              final Dataset values = useA ? db : da;
              final IDataset masterIndices = useA ? aIndices : bIndices;
              final IDataset masterValues = useA ? da : db;
              
              
              // ok, now do interpolation
              Dataset interpolatedValues = Maths.interpolate(indices, values,
                  masterIndices, null, null);
              operandA = masterValues;
              operandB = interpolatedValues;
              
              // remember the output axes, since we'll put them into the results
              interpolatedValues.addMetadata(masterValues.getFirstMetadata(AxesMetadata.class));
            }
            else
            {
              // an exception was thrown while trying to determine the 
              // indices. See if the parent class can handle it
              operandA = da;
              operandB = db;
            }
          }
          else
          {
            // no interpolation needed
            operandA = da;
            operandB = db;
          }
        }
        else
        {
          // we don't know the axes, so forget about syncing
          operandA = da;
          operandB = db;
        }
      }
      else
      {
        // we can only handle datasets, leave them to it.
        operandA = a;
        operandB = b;
      }

      // perform the basic operation
      final Dataset res = Maths.add(operandA, operandB, o);

      // ok, we'll use this as the target metadata
      if (operandA instanceof IDataset)
      {
        final IDataset ds = (IDataset) operandA;
        final AxesMetadata targetAxes = ds.getFirstMetadata(AxesMetadata.class);
        if (targetAxes != null)
        {
          res.addMetadata(targetAxes);
        }
      }

      return res;
    }

    public static Dataset interpolate(final Dataset x, final Dataset d,
        final IDataset x0, Number left, Number right)
    {

      // ok, do the math
      final Dataset res = Maths.interpolate(x, d, x0, left, right);

      // ok, we'll use this as the target metadata
      AxesMetadata targetAxes = new AxesMetadataImpl();
      targetAxes.initialize(1);
      targetAxes.setAxis(0, x0);
      res.addMetadata(targetAxes);

      return res;
    }

  }

  @Before
  public void before()
  {
    // prevent missing logger from being reported
    Utils.suppressSLF4JError();
  }

  @Test
  public void testSyncedOperations()
  {
    // configure Dataset A
    Dataset timestampsA = DatasetFactory.createFromList(Arrays.asList(100l,
        200l, 300l));
    Dataset speedsA = DatasetFactory.createFromList(Arrays.asList(1d, 2d, 3d));
    AxesMetadata axesMetadataA = new AxesMetadataImpl();
    axesMetadataA.initialize(1);
    axesMetadataA.setAxis(0, timestampsA);
    speedsA.addMetadata(axesMetadataA);
    speedsA.setName("speedsA");

    // configure Dataset B
    Dataset timestampsB = DatasetFactory.createFromList(Arrays.asList(100l,
        200l, 300l));
    Dataset speedsB = DatasetFactory.createFromList(Arrays.asList(2d, 4d, 8d));
    AxesMetadata axesMetadataB = new AxesMetadataImpl();
    axesMetadataB.initialize(1);
    axesMetadataB.setAxis(0, timestampsB);
    speedsB.addMetadata(axesMetadataB);
    speedsB.setName("speedsB");

    // ok, we need them to be of equal length
    assertEquals("times equal length", timestampsA.getSize(), timestampsB
        .getSize());
    assertEquals("speeds equal length", speedsA.getSize(), speedsB.getSize());

    // verify we have the correct length of axis metadata
    assertEquals("A components equal length", timestampsA.getSize(), speedsA
        .getSize());
    assertEquals("B components equal length", timestampsB.getSize(), speedsB
        .getSize());

    header("Simple operations");

    // show initial values
    printTimedDataset(speedsA);
    printTimedDataset(speedsB);

    // non-destructive addition (loses units)
    Dataset sumAB = Maths.add(speedsA, speedsB);
    Dataset productAB = Maths.multiply(speedsA, speedsB);

    // check the results
    assertEquals("Results correct length", sumAB.getSize(), speedsA.getSize());
    assertEquals("Results correct length", productAB.getSize(), speedsA
        .getSize());

    // output results
    printTimedDataset(sumAB);
    printTimedDataset(productAB);
  }

  @Test
  public void testNonSyncedAxesOperation()
  {
    // configure Dataset A
    Dataset timestampsA = DatasetFactory.createFromList(Arrays.asList(100l,
        300l, 500l));
    timestampsA.setName("A timestamps");
    Dataset speedsA = DatasetFactory.createFromList(Arrays.asList(1d, 2d, 3d));
    AxesMetadata axesMetadataA = new AxesMetadataImpl();
    axesMetadataA.initialize(1);
    axesMetadataA.setAxis(0, timestampsA);
    speedsA.addMetadata(axesMetadataA);
    speedsA.setName("speedsA, with timestamps");

    // configure Dataset B
    Dataset timestampsB = DatasetFactory.createFromList(Arrays.asList(150l,
        200l, 250l, 300L, 350L));
    timestampsB.setName("B timestamps");
    Dataset speedsB = DatasetFactory.createFromList(Arrays.asList(2d, 4d, 8d,
        12d, 14d));
    AxesMetadata axesMetadataB = new AxesMetadataImpl();
    axesMetadataB.initialize(1);
    axesMetadataB.setAxis(0, timestampsB);
    speedsB.addMetadata(axesMetadataB);
    speedsB.setName("speedsB, with timestamps");

    header("Non synced operations");

    // show initial values
    printTimedDataset(speedsA);
    printTimedDataset(speedsB);

    // ok - verify they're not synced to start with
    assertNotEquals("times different length", timestampsA.getSize(), timestampsB
        .getSize());
    assertNotEquals("speeds different length", speedsA.getSize(), speedsB
        .getSize());

    // verify we have the correct length of axis metadata
    assertEquals("A components equal length", timestampsA.getSize(), speedsA
        .getSize());
    assertEquals("B components equal length", timestampsB.getSize(), speedsB
        .getSize());

    // check addition fails for dataset of unequal length
    try
    {
      Maths.add(speedsA, speedsB);
      fail("An exception should have been thrown");
    }
    catch (IllegalArgumentException ex)
    {
      assertEquals("Correct reason",
          "A shape's dimension was not one or equal to maximum", ex
              .getMessage());
    }

    // ok, produce interpolated datasets
    Dataset speedsC = Maths.interpolate(timestampsA, speedsA, timestampsB, null,
        null);
    speedsC.setName("A values at B timestamps");
    printTimedDataset(speedsC);

    assertEquals("speeds equal length", speedsC.getSize(), speedsB.getSize());

    // check results, via sampling
    assertEquals("correct value", 1.5d, speedsC.getDouble(1), 0.001); // A speed at 200
    assertEquals("correct value", 1.75d, speedsC.getDouble(2), 0.001); // A speed at 250

    // put the timestamps back in
    speedsC.addMetadata(axesMetadataB);
    speedsC.setName("A values at B timestamps, with timestamp metadata");
    printTimedDataset(speedsC);

    // now add them
    Dataset sumBC = Maths.add(speedsB, speedsC);
    sumBC.setName("Sum of A and B, taken at B timestamps");
    printTimedDataset(sumBC);

    // put the axes metadata into the results
    sumBC.addMetadata(axesMetadataB);
    sumBC.setName(
        "Sum of A(C) and B, taken at B timestamps, with timestamp metadata");
    printTimedDataset(sumBC);
  }

  @Test
  public void testNonSyncedAxesOperationInNewMaths()
  {
    // configure Dataset A
    Dataset timestampsA = DatasetFactory.createFromList(Arrays.asList(100l,
        300l, 500l));
    timestampsA.setName("A timestamps");
    Dataset speedsA = DatasetFactory.createFromList(Arrays.asList(1d, 2d, 3d));
    AxesMetadata axesMetadataA = new AxesMetadataImpl();
    axesMetadataA.initialize(1);
    axesMetadataA.setAxis(0, timestampsA);
    speedsA.addMetadata(axesMetadataA);
    speedsA.setName("speedsA, with timestamps");

    // configure Dataset B
    Dataset timestampsB = DatasetFactory.createFromList(Arrays.asList(150l,
        200l, 250l, 300L, 350L));
    timestampsB.setName("B timestamps");
    Dataset speedsB = DatasetFactory.createFromList(Arrays.asList(2d, 4d, 8d,
        12d, 14d));
    AxesMetadata axesMetadataB = new AxesMetadataImpl();
    axesMetadataB.initialize(1);
    axesMetadataB.setAxis(0, timestampsB);
    speedsB.addMetadata(axesMetadataB);
    speedsB.setName("speedsB, with timestamps");

    header("Non synced operations");

    // show initial values
    printTimedDataset(speedsA);
    printTimedDataset(speedsB);

    // ok - verify they're not synced to start with
    assertNotEquals("times different length", timestampsA.getSize(), timestampsB
        .getSize());
    assertNotEquals("speeds different length", speedsA.getSize(), speedsB
        .getSize());

    // verify we have the correct length of axis metadata
    assertEquals("A components equal length", timestampsA.getSize(), speedsA
        .getSize());
    assertEquals("B components equal length", timestampsB.getSize(), speedsB
        .getSize());

    // check addition fails for dataset of unequal length
    try
    {
      Maths.add(speedsA, speedsB);
      fail("An exception should have been thrown");
    }
    catch (IllegalArgumentException ex)
    {
      assertEquals("Correct reason",
          "A shape's dimension was not one or equal to maximum", ex
              .getMessage());
    }

    // ok, produce interpolated datasets
    Dataset speedsC = NewMaths.interpolate(timestampsA, speedsA, timestampsB,
        null, null);
    speedsC.setName("A values at B timestamps");
    printTimedDataset(speedsC);

    assertEquals("speeds equal length", speedsC.getSize(), speedsB.getSize());
    final AxesMetadata speedsCAxes = speedsC.getFirstMetadata(
        AxesMetadata.class);
    assertNotNull("timestamps present in results", speedsCAxes);
    assertEquals("correct timestamps", timestampsB, speedsCAxes.getAxis(0)[0]);

    // check results, via sampling
    assertEquals("correct value", 1.5d, speedsC.getDouble(1), 0.001); // A speed at 200
    assertEquals("correct value", 1.75d, speedsC.getDouble(2), 0.001); // A speed at 250

    // now add them
    Dataset sumBC = NewMaths.add(speedsB, speedsC, null);
    sumBC.setName("Sum of A and B, taken at B timestamps");
    printTimedDataset(sumBC);
    AxesMetadata sumAxes = sumBC.getFirstMetadata(AxesMetadata.class);
    assertNotNull("timestamps present in results", sumAxes);
    assertEquals("correct timestamps", timestampsB, sumAxes.getAxis(0)[0]);

    // check results, via sampling
    assertEquals("correct value", 5.5d, sumBC.getDouble(1), 0.001); // speed sum at 200
    assertEquals("correct value", 9.75d, sumBC.getDouble(2), 0.001); // speed sum at 250
  }

  @Test
  public void testNonSyncedAxesOperationInNewMaths2()
  {
    // this test verifies that the add operation interpolates the
    // non-synced input data

    // configure Dataset A
    Dataset timestampsA = DatasetFactory.createFromList(Arrays.asList(100l,
        300l, 500l));
    timestampsA.setName("A timestamps");
    Dataset speedsA = DatasetFactory.createFromList(Arrays.asList(1d, 2d, 3d));
    AxesMetadata axesMetadataA = new AxesMetadataImpl();
    axesMetadataA.initialize(1);
    axesMetadataA.setAxis(0, timestampsA);
    speedsA.addMetadata(axesMetadataA);
    speedsA.setName("speedsA, with timestamps");

    // configure Dataset B
    Dataset timestampsB = DatasetFactory.createFromList(Arrays.asList(150l,
        200l, 250l, 300L, 350L));
    timestampsB.setName("B timestamps");
    Dataset speedsB = DatasetFactory.createFromList(Arrays.asList(2d, 4d, 8d,
        12d, 14d));
    AxesMetadata axesMetadataB = new AxesMetadataImpl();
    axesMetadataB.initialize(1);
    axesMetadataB.setAxis(0, timestampsB);
    speedsB.addMetadata(axesMetadataB);
    speedsB.setName("speedsB, with timestamps");

    header("Non synced operations (auto interpolation)");

    // show initial values
    printTimedDataset(speedsA);
    printTimedDataset(speedsB);

    // ok - verify they're not synced to start with
    assertNotEquals("times different length", timestampsA.getSize(), timestampsB
        .getSize());
    assertNotEquals("speeds different length", speedsA.getSize(), speedsB
        .getSize());

    // verify we have the correct length of axis metadata
    assertEquals("A components equal length", timestampsA.getSize(), speedsA
        .getSize());
    assertEquals("B components equal length", timestampsB.getSize(), speedsB
        .getSize());

    // check addition fails for dataset of unequal length
    try
    {
      Maths.add(speedsA, speedsB);
      fail("An exception should have been thrown");
    }
    catch (IllegalArgumentException ex)
    {
      assertEquals("Correct reason",
          "A shape's dimension was not one or equal to maximum", ex
              .getMessage());
    }

    // now add them, without doing interpolation first
    Dataset sumBC = NewMaths.add2(speedsA, speedsB, null);
    sumBC.setName("Sum of A and B, taken at B timestamps");
    printTimedDataset(sumBC);
    AxesMetadata sumAxes = sumBC.getFirstMetadata(AxesMetadata.class);
    assertNotNull("timestamps present in results", sumAxes);
    assertEquals("correct timestamps", timestampsB, sumAxes.getAxis(0)[0]);

    // check results, via sampling
    assertEquals("correct value", 5.5d, sumBC.getDouble(1), 0.001); // speed sum at 200
    assertEquals("correct value", 9.75d, sumBC.getDouble(2), 0.001); // speed sum at 250
  }

  public void header(String title)
  {
    System.out.println();
    System.out.println("=== " + title + " ===");
  }

  public void printTimedDataset(Dataset dataset)
  {
    final AxesMetadata axesMetadata = dataset.getFirstMetadata(
        AxesMetadata.class);
    IndexIterator iterator = dataset.getIterator();

    final LongDataset axisDataset;
    if (axesMetadata != null && axesMetadata.getAxes().length > 0)
    {
      axisDataset = (LongDataset) axesMetadata.getAxes()[0];
    }
    else
    {
      axisDataset = null;
    }

    System.out.println(dataset.getName() + ":");
    while (iterator.hasNext())
    {
      final String indexVal;
      if (axisDataset != null)
      {
        indexVal = "" + axisDataset.getLong(iterator.index);
      }
      else
      {
        indexVal = "N/A";
      }

      System.out.print(indexVal + " : " + dataset.getDouble(iterator.index));
      System.out.print("; ");
    }
    System.out.println();
  }

}