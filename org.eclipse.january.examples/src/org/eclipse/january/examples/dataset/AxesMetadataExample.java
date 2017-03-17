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

public class AxesMetadataExample {

	public static void main(String[] args) {
		testDataset();
		testCompoundDataset();
	}

	private static void testDataset() {
		// configure Dataset A
		Dataset timestampsA = DatasetFactory.createFromList(Arrays.asList(100l, 200l, 300l));
		timestampsA.addMetadata(new UomMetadata(SI.SECOND));
		Dataset speedsA = DatasetFactory.createFromList(Arrays.asList(1d, 2d, 3d));
		AxesMetadata axesMetadataA = new AxesMetadataImpl();
		axesMetadataA.initialize(1);
		axesMetadataA.setAxis(0, timestampsA);
		speedsA.addMetadata(axesMetadataA);
		speedsA.addMetadata(new UomMetadata(SI.METRES_PER_SECOND));
		speedsA.setName("speedsA");

		// configure Dataset B
		Dataset timestampsB = DatasetFactory.createFromList(Arrays.asList(100l, 200l, 300l));
		timestampsB.addMetadata(new UomMetadata(SI.SECOND));
		Dataset speedsB = DatasetFactory.createFromList(Arrays.asList(2d, 4d, 8d));
		AxesMetadata axesMetadataB = new AxesMetadataImpl();
		axesMetadataB.initialize(1);
		axesMetadataB.setAxis(0, timestampsB);
		speedsB.addMetadata(axesMetadataB);
		speedsB.addMetadata(new UomMetadata(SI.METRES_PER_SECOND));
		speedsB.setName("speedsB");

		// show initial values
		printTimedDataset(speedsA);
		printTimedDataset(speedsB);

		// non-destructive addition (loses units)
		Dataset sumAB2 = Maths.add(speedsA, speedsB);
		Dataset productAB2 = Maths.multiply(speedsA, speedsB);

		// destructive addition (amends speedsA)
		Dataset sumAB = speedsA.iadd(speedsB);
		Dataset productAB = speedsA.iadd(speedsB);

		// output results
		printTimedDataset(speedsA);
		printTimedDataset(sumAB);
		printTimedDataset(productAB);
		printTimedDataset(sumAB2);
		printTimedDataset(productAB2);
	}

	private static void testCompoundDataset() {

		// configure Dataset A
		Dataset speedsA = DatasetFactory.createFromList(Arrays.asList(1d, 2d, 3d));
		speedsA.addMetadata(new UomMetadata(SI.METRES_PER_SECOND));
		speedsA.setName("speedsA");

		Dataset engineTemperatureA = DatasetFactory.createFromList(Arrays.asList(56d, 58d, 62d));
		engineTemperatureA.addMetadata(new UomMetadata(SI.CELSIUS));
		engineTemperatureA.setName("engineTemperatureA");

		Dataset fuelTankVolumeA = DatasetFactory.createFromList(Arrays.asList(0.3d, 0.28d, 0.22d));
		fuelTankVolumeA.addMetadata(new UomMetadata(SI.CUBIC_METRE));
		fuelTankVolumeA.setName("fuelTankVolumeA");

		CompoundDataset compoundDatasetA = DatasetFactory.createCompoundDataset(speedsA, engineTemperatureA,
				fuelTankVolumeA);
		AxesMetadata axesMetadataA = new AxesMetadataImpl();
		axesMetadataA.initialize(1);
		Dataset timestampsA = DatasetFactory.createFromList(Arrays.asList(100l, 200l, 300l));
		timestampsA.addMetadata(new UomMetadata(SI.SECOND));
		axesMetadataA.setAxis(0, timestampsA);
		compoundDatasetA.addMetadata(axesMetadataA);
		compoundDatasetA.setName("compoundDatasetA");

		// configure Dataset B
		Dataset speedsB = DatasetFactory.createFromList(Arrays.asList(2d, 4d, 8d));
		speedsB.addMetadata(new UomMetadata(SI.METRES_PER_SECOND));
		speedsB.setName("speedsB");

		Dataset engineTemperatureB = DatasetFactory.createFromList(Arrays.asList(12d, 34d, 35d));
		engineTemperatureB.addMetadata(new UomMetadata(SI.CELSIUS));
		engineTemperatureB.setName("engineTemperatureB");

		Dataset fuelTankVolumeB = DatasetFactory.createFromList(Arrays.asList(0.5d, 0.46d, 0.43d));
		fuelTankVolumeB.addMetadata(new UomMetadata(SI.CUBIC_METRE));
		fuelTankVolumeB.setName("fuelTankVolumeB");

		CompoundDataset compoundDatasetB = DatasetFactory.createCompoundDataset(speedsB, engineTemperatureB,
				fuelTankVolumeB);
		AxesMetadata axesMetadataB = new AxesMetadataImpl();
		axesMetadataB.initialize(1);
		Dataset timestampsB = DatasetFactory.createFromList(Arrays.asList(100l, 200l, 300l));
		timestampsB.addMetadata(new UomMetadata(SI.SECOND));
		axesMetadataB.setAxis(0, timestampsB);
		compoundDatasetB.addMetadata(axesMetadataB);
		compoundDatasetB.setName("compoundDatasetB");

		// Add B to A
		CompoundDataset sumAB = compoundDatasetA.iadd(compoundDatasetB);
		sumAB.setName("sumAB");

		printTimedCompoundDataset(sumAB);
	}

	public static void printTimedDataset(Dataset dataset) {
		printTimedDataset(dataset, dataset.getFirstMetadata(AxesMetadata.class));
	}

	public static void printTimedDataset(Dataset dataset, AxesMetadata axesMetadata) {
		IndexIterator iterator = dataset.getIterator();
		UomMetadata uomMetadata = dataset.getFirstMetadata(UomMetadata.class);
		String unit = uomMetadata != null ? uomMetadata.getUnit().toString() : "";
		
		final String axisUnit;
		final LongDataset axisDataset;
		if(axesMetadata != null && axesMetadata.getAxes().length > 0)
		{
			axisDataset = (LongDataset) axesMetadata.getAxes()[0];
			UomMetadata axusUomMetadata = axisDataset.getFirstMetadata(UomMetadata.class);
			axisUnit = axusUomMetadata != null ? axusUomMetadata.getUnit().toString() : "";
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
			if(axisDataset != null)
			{
				indexVal = axisDataset.getLong(iterator.index) + "[" + axisUnit + "]";
			}
			else
			{	
				indexVal = "N/A";
			}
	
			System.out.print(indexVal + " : "
					+ dataset.getDouble(iterator.index) + "[" + unit + "]");
			System.out.print("; ");
		}
		System.out.println();
	}

	public static void printTimedCompoundDataset(CompoundDataset compoundDataset) {
		AxesMetadata axesMetadata = compoundDataset.getFirstMetadata(AxesMetadata.class);
		int size = compoundDataset.getElementsPerItem();
		System.out.println(compoundDataset.getName() + "{");
		for (int i = 0; i < size; i++) {
			System.out.print("\t");
			printTimedDataset(compoundDataset.getElements(i), axesMetadata);
		}
		System.out.println("}");
	}
}