package models;

import java.util.HashMap;

public class GNN {
	
	public static class Tensor {
		private double[] values;
		public Tensor(String expr) {
			if(expr.length()==0) {
				values = new double[0];
				return;
			}
			String[] splt = expr.split(",");
			values = new double[splt.length];
			for(int i=0;i<splt.length;i++)
				put(i, Double.parseDouble(splt[i]));
		}
		public Tensor(int size) {
			values = new double[size];
		}
		public void randomize() {
			for(int i=0;i<size();i++)
				put(i, Math.random());
		}
		public void put(int pos, double value) {
			values[pos] = value;
		}
		public void putAdd(int pos, double value) {
			put(pos, get(pos)+value);
		}
		public int size() {
			return values.length;
		}
		public Tensor zero() {
			return new Tensor(values.length);
		}
		public void assertSize(int size) {
			if(size()!=size)
				throw new RuntimeException("Different sizes: given "+size+" vs "+size());
		}
		public Tensor add(Tensor tensor) {
			assertSize(tensor.size());
			Tensor res = zero();
			for(int i=0;i<values.length;i++)
				values[i] += tensor.values[i];
			return res;
		}
		public Tensor subtract(Tensor tensor) {
			assertSize(tensor.size());
			Tensor res = zero();
			for(int i=0;i<values.length;i++)
				res.put(i, get(i)-tensor.get(i));
			return res;
		}
		public Tensor multiply(Tensor tensor) {
			assertSize(tensor.size());
			Tensor res = zero();
			for(int i=0;i<values.length;i++)
				res.put(i, get(i)*tensor.get(i));
			return res;
		}
		public Tensor multiply(double value) {
			Tensor res = zero();
			for(int i=0;i<values.length;i++)
				res.put(i, get(i)*value);
			return res;
		}
		public double get(int pos) {
			return values[pos];
		}
		public double dot(Tensor tensor) {
			assertSize(tensor.size());
			double res = 0;
			for(int i=0;i<values.length;i++)
				res += get(i)*tensor.get(i);
			return res;
		}
		public double dot(Tensor tensor1, Tensor tensor2) {
			assertSize(tensor1.size());
			assertSize(tensor2.size());
			double res = 0;
			for(int i=0;i<values.length;i++)
				res += get(i)*tensor1.get(i)*tensor2.get(i);
			return res;
		}
		public double norm() {
			double res = 0;
			for(double value : values)
				res += value*value;
			return Math.sqrt(res);
		}
		public String toString() {
			StringBuilder res = new StringBuilder();
			if(size()!=0)
				res.append(get(0));
			for(int i=1;i<size();i++)
				res.append(",").append(get(i));
			return res.toString();
		}
		public void normalize() {
			double norm = norm();
			if(norm!=0)
				for(int i=0;i<values.length;i++)
					put(i, get(i)/norm);
		}
		public void uniformize() {
			for(int i=0;i<values.length;i++)
				put(i, 1./values.length);
		}
		public void setOnes() {
			for(int i=0;i<values.length;i++)
				put(i, 1.);
		}
	}
	
	public static class Matrix {
		protected double[] W;
		protected int inputSize;
		protected int outputSize;
		public Matrix(int inputSize, int outputSize) {
			this.inputSize = inputSize;
			this.outputSize = outputSize;
			W = new double[inputSize*outputSize];
		}
		public void randomize() {
			for(int i=0;i<W.length;i++)
				W[i] = Math.random();
		}
		public void setZero() {
			for(int i=0;i<W.length;i++)
				W[i] = 0;
		}
		public double getW(int inputPos, int outputPos) {
			if(outputPos>=outputSize)
				throw new RuntimeException("Output position should be less than "+outputSize);
			return W[inputPos*outputSize+outputPos];
		}
		public void putW(int inputPos, int outputPos, double value) {
			if(outputPos>=outputSize)
				throw new RuntimeException("Output position should be less than "+outputSize);
			 W[inputPos*outputSize+outputPos] = value;
		}
		public void addW(int inputPos, int outputPos, double value) {
			putW(inputPos, outputPos, getW(inputPos, outputPos) + value);
		}
		public Tensor multiply(Tensor input) {
			input.assertSize(inputSize);
			Tensor output = new Tensor(outputSize);
			for(int i=0;i<inputSize;i++)
				for(int j=0;j<outputSize;j++)
					output.putAdd(j, getW(i, j)*input.get(i));
			return output;
		}
		public double norm() {
			double sum = 0;
			for(double value : W) 
				sum += value*value;
			return Math.sqrt(sum);
		}
		public void normalize() {
			double norm = norm();
			if(norm==0)
				return;
			for(int i=0;i<W.length;i++)
				W[i] /= norm;
		}
	}
	
	public static class MatrixWithTape extends Matrix {
		private Matrix tape;
		private HashMap<Tensor, Tensor> history = new HashMap<Tensor, Tensor>();
		public MatrixWithTape(int inputSize, int outputSize) {
			super(inputSize, outputSize);
			tape = null;
		}
		public Tensor lastOutput(Tensor input) {
			return history.get(input);
		}
		@Override
		public Tensor multiply(Tensor input) {
			Tensor output = super.multiply(input);
			history.put(input, output);
			return output;
		}
		public void startTape() {
			tape = new Matrix(inputSize, outputSize);
		}
		public void accumulateError(Tensor input, Tensor error) {
			if(tape==null)
				throw new RuntimeException("Must start tape before accumulating error");
			Tensor output = history.get(input);
			if(output==null)
				throw new RuntimeException("Needs to run once before accumulating error for an input");
			for(int i=0;i<inputSize;i++)
				for(int j=0;j<outputSize;j++) 
					tape.addW(i, j, input.get(i));
		}
		public void train(double learningRate, double regularization) {
			if(tape==null)
				throw new RuntimeException("Must start tape and accumulate error before training");
			//tape.normalize();
			for(int i=0;i<W.length;i++)
				W[i] += tape.W[i]*learningRate - W[i]*regularization;
			//System.out.println(tape.norm());
			history.clear();
			tape.setZero();
		}
	}
	
	
	public static class Relation {
		private MatrixWithTape matrix;
		private Tensor relation;
		private Tensor relationDerivative;
		private int inputDims;
		private int embeddingDims;
		private int accumulationSize = 0;
		
		public Relation(int inputDims, int embeddingDims) {
			this.inputDims = inputDims;
			this.embeddingDims = embeddingDims;
			relation = new Tensor(embeddingDims);
			matrix = new MatrixWithTape(inputDims, embeddingDims);
			matrix.randomize();
			relation.setOnes();
		}
		private double activate(Tensor input1, Tensor input2) {
			input1.assertSize(inputDims);
			input2.assertSize(inputDims);
			return relation.dot(matrix.multiply(input1).multiply(matrix.multiply(input2)));
		}
		public double predict(Tensor input1, Tensor input2) {
			return 1./(1+Math.exp(-activate(input1, input2)));
		}
		public void startTraining() {
			matrix.startTape();
			relationDerivative = relation.zero();
			accumulationSize = 0;
		}
		public Tensor multiply(Tensor ego) {
			Tensor res = matrix.multiply(ego);
			res.normalize();
			return res;
		}
		public void accumulateCrossEntropy(Tensor input1, Tensor input2, double target, double weight) {
			startTraining();
			double activation = activate(input1, input2);
			double output = 1./(1+Math.exp(-activation));
			double partialEntropy = (1-target)*output - target*(1-output); // D entropy / D activation
			partialEntropy *= weight;
			System.out.println(partialEntropy*input2.norm()+" "+output+" "+activation+" "+target);
			matrix.accumulateError(input1, matrix.multiply(input2).multiply(relation).multiply(partialEntropy));
			matrix.accumulateError(input2, matrix.multiply(input1).multiply(relation).multiply(partialEntropy));
			relationDerivative.add(matrix.multiply(input1).multiply(matrix.multiply(input2)).multiply(partialEntropy));
			accumulationSize += 1;
			train();
		}
		public void train() {
			if(accumulationSize!=0)
				matrix.train(0.1, 0);
			//relation.add(relationDerivative.multiply(-0.001));
			//relation.add(relation.multiply(-0.1));
			relationDerivative = null;
			accumulationSize = 0;
		}
	}
}
