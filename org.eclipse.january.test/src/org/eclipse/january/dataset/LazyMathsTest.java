/*-
 * Copyright (c) 2012, 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.january.dataset;

import org.eclipse.january.asserts.TestUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.LazyMaths;
import org.eclipse.january.dataset.Maths;
import org.junit.Test;

public class LazyMathsTest {

	@Test
	public void testSum() throws Exception {
		Dataset a = DatasetFactory.createRange(100, Dataset.FLOAT64);

		TestUtils.assertDatasetEquals(a.sum(0), LazyMaths.sum(a, 0), 1e-9, 1e-15);

		a.setShape(10, 10);
		TestUtils.assertDatasetEquals(a.sum(0), LazyMaths.sum(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1), LazyMaths.sum(a, 1), 1e-9, 1e-15);

		a.setShape(4, 5, 5);
		TestUtils.assertDatasetEquals(a.sum(0), LazyMaths.sum(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1), LazyMaths.sum(a, 1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(2), LazyMaths.sum(a, 2), 1e-9, 1e-15);

		a.setShape(4, 5, 1, 5);
		TestUtils.assertDatasetEquals(a.sum(0), LazyMaths.sum(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1), LazyMaths.sum(a, 1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(2), LazyMaths.sum(a, 2), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(3), LazyMaths.sum(a, 3), 1e-9, 1e-15);
	}

	@Test
	public void testSumIgnoreAxes() throws Exception {
		Dataset a = DatasetFactory.createRange(100, Dataset.FLOAT64);

		a.setShape(10, 10);
		// force the use of the varargs sum method by using 1-element arrays
		TestUtils.assertDatasetEquals(a.sum(0), LazyMaths.sum(a, new int[]{1}), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1), LazyMaths.sum(a, new int[]{0}), 1e-9, 1e-15);

		a.setShape(4, 5, 5);
		TestUtils.assertDatasetEquals(a.sum(0), LazyMaths.sum(a, 1, 2), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1), LazyMaths.sum(a, 0, 2), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(2), LazyMaths.sum(a, 0, 1), 1e-9, 1e-15);

		a.setShape(4, 5, 1, 5);
		TestUtils.assertDatasetEquals(a.sum(0), LazyMaths.sum(a, 1, 2, 3), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1), LazyMaths.sum(a, 0, 2, 3), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(2), LazyMaths.sum(a, 0, 1, 3), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(3), LazyMaths.sum(a, 0, 1, 2), 1e-9, 1e-15);
		
		TestUtils.assertDatasetEquals(a.sum(0).sum(0), LazyMaths.sum(a, false, 0, 1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1).sum(0), LazyMaths.sum(a, false, 1, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(1).sum(0), LazyMaths.sum(a, false, 0, 1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.sum(3).sum(2).sum(0), LazyMaths.sum(a, false, 0, 3, 2), 1e-9, 1e-15);
	}

	@Test
	public void testProduct() throws Exception {
		Dataset a = DatasetFactory.createRange(100, Dataset.FLOAT64);
		a.iadd(1.);
		a.idivide(100.);

		TestUtils.assertDatasetEquals(a.product(0), LazyMaths.product(a, 0), 1e-9, 1e-15);

		a.setShape(10, 10);
		TestUtils.assertDatasetEquals(a.product(0), LazyMaths.product(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.product(1), LazyMaths.product(a, 1), 1e-9, 1e-15);

		a.setShape(4, 5, 5);
		TestUtils.assertDatasetEquals(a.product(0), LazyMaths.product(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.product(1), LazyMaths.product(a, 1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.product(2), LazyMaths.product(a, 2), 1e-9, 1e-15);

		a.setShape(4, 5, 1, 5);
		TestUtils.assertDatasetEquals(a.product(0), LazyMaths.product(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.product(1), LazyMaths.product(a, 1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.product(2), LazyMaths.product(a, 2), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.product(3), LazyMaths.product(a, 3), 1e-9, 1e-15);
	}
	
	@Test
	public void testMeanIgnore() throws Exception {

		Dataset a = DatasetFactory.createRange(100, Dataset.FLOAT64);
		a.setShape(10, 10);
		TestUtils.assertDatasetEquals(a.mean(1), LazyMaths.mean(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.mean(0), LazyMaths.mean(a, 1), 1e-9, 1e-15);

		a.setShape(4, 5, 5);
		TestUtils.assertDatasetEquals(a.mean(2).mean(1),LazyMaths.mean(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.mean(2),LazyMaths.mean(a, 0,1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.mean(0),LazyMaths.mean(a, 1,2), 1e-9, 1e-15);

		a.setShape(4, 5, 1, 5);
		TestUtils.assertDatasetEquals(a.mean(3).mean(2),LazyMaths.mean(a, 0,1), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.mean(3).mean(2).mean(1),LazyMaths.mean(a, 0), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.mean(3).squeeze(),LazyMaths.mean(a, 0,1,2), 1e-9, 1e-15);
		TestUtils.assertDatasetEquals(a.mean(0).mean(0).squeeze(),LazyMaths.mean(a, 2,3), 1e-9, 1e-15);
		
		Dataset er = DatasetFactory.createRange(100, Dataset.FLOAT64);
		a.setShape(10, 10);
		er.setShape(10, 10);
		a.setErrorBuffer(er);

		Dataset e = Maths.sqrt(er.sum(1)).idivide(10);

		TestUtils.assertDatasetEquals(e, LazyMaths.mean(a, 0).getErrors(), 1e-9, 1e-15);

	}
}
