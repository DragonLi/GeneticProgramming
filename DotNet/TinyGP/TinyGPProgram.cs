using System;
using System.IO;

namespace TinyGP
{
    internal static class TinyGpProgram
    {
        public static int Main(string[] args)
        {
            var fname = "problem.dat";
            var s = -1;

            if (args.Length >= 2)
            {
                s = int.Parse(args[0]);
                fname = args[1];
            }

            if (args.Length == 1) fname = args[0];

            var gp = new TinyGp(fname, s);
            return gp.Evolve();
        }
    }

    internal class TinyGp
    {
        private const byte
            Add = 110,
            Sub = 111,
            Mul = 112,
            Div = 113,
            FsetStart = Add,
            FsetEnd = Div;

        private const int
            MaxLen = 10000,
            PopSize = 100000,
            Depth = 5,
            Generations = 100,
            SampleSize = 2;

        private const double
            ProbPointMutPerNode = 0.05,
            CrossoverProb = 0.9;

        private const double SymbolicRegressionBestFit = -1e-5;

        /// store values of input variables or random constants
        private static readonly double[] X = new double[FsetStart];

        private static double _minRandom, _maxRandom;
        private static byte[] _program;
        private static int _pc;
        private static int _varNumber, _fitnesscases, _randomNumber;
        private static int _seed;
        private static Random _rd = new Random();
        private static double[][] _targets;

        private static readonly byte[] Buffer = new byte[MaxLen];

        private readonly char[] _separator = {' ', '\t', '\n', '\r', '\f'};

        private readonly double[] _fitness;
        private readonly byte[][] _pop;
        
        //first efficiency improvement,cache end index of each node
        private int[][] _popNodeEndIndex;

        public TinyGp(string fname, int s)
        {
            _fitness = new double[PopSize];
            _seed = s;
            if (_seed >= 0)
                _rd = new Random(_seed);
            setup_fitness(fname);
            for (var i = _varNumber; i < FsetStart; i++)
                X[i] = (_maxRandom - _minRandom) * _rd.NextDouble() + _minRandom;
            _pop = create_random_pop(PopSize, Depth, _fitness);
        }

        ///read parameters and input/output examples from input file
        private void setup_fitness(string fname)
        {
            var isHeader = true;
            var targetIndex = 0;
            foreach (var line in File.ReadLines(fname))
            {
                var tokenList = line.Split(_separator, StringSplitOptions.RemoveEmptyEntries);
                if (isHeader)
                {
                    isHeader = false;
                    var tokenIndex = 0;
                    _varNumber = int.Parse(tokenList[tokenIndex++]);
                    _randomNumber = int.Parse(tokenList[tokenIndex++]);
                    _minRandom = double.Parse(tokenList[tokenIndex++]);
                    _maxRandom = double.Parse(tokenList[tokenIndex++]);
                    _fitnesscases = int.Parse(tokenList[tokenIndex]);
                    _targets = new double[_fitnesscases][];
                    if (_varNumber + _randomNumber >= FsetStart)
                        Console.WriteLine("too many variables and constants");
                }
                else
                {
                    _targets[targetIndex] = new double[_varNumber + 1];
                    for (var j = 0; j <= _varNumber; j++) _targets[targetIndex][j] = double.Parse(tokenList[j]);
                    targetIndex++;
                }
            }
        }

        private byte[][] create_random_pop(int popsize, int depth, double[] pFitness)
        {
            var pop = new byte[popsize][];
            _popNodeEndIndex = new int[popsize][];

            for (var i = 0; i < popsize; i++)
            {
                pop[i] = create_random_indiv(depth);
                pFitness[i] = fitness_function(pop[i]);
                _popNodeEndIndex[i]=new int[pop[i].Length];
                TraverseAndSetNodeEndIndex(pop[i],0,_popNodeEndIndex[i]);
                TraverseSlow(pop[i], 0, _popNodeEndIndex[i]);
            }

            return pop;
        }

        private byte[] create_random_indiv(int depth)
        {
            var len = Grow(Buffer, 0, MaxLen, depth);

            while (len < 0) //since 0 always < MAX_LEN, len<0 means buffer represents an incorrect tree
                len = Grow(Buffer, 0, MaxLen, depth);

            var ind = new byte[len];

            System.Buffer.BlockCopy(Buffer, 0, ind, 0, len);
            return ind;
        }

        ///return next position to grow (equal to the length occupied), or -1 to signal the end of grow because of exceeding max len or incorrect number of operand
        private int Grow(byte[] pBuffer, int position, int maxLen, int depth)
        {
            while (true)
            {
                if (position >= maxLen) return -1;

                var prim = position == 0
                    ? (byte) 1
                    : (byte) _rd.Next(
                        2); //50% operator,50% random constant or variable (input parameters), and force the root node must be an operator

                if (prim == 0 || depth == 0 || position + 2 >= maxLen
                ) //if depth is exceeded or there is no room for an operand
                {
                    prim = (byte) _rd.Next(
                        _varNumber + _randomNumber); //refer to one of input parameters or random constants
                    pBuffer[position] = prim;
                    return position + 1;
                }

                prim = (byte) (_rd.Next(FsetEnd - FsetStart + 1) + FsetStart);
                switch (prim)
                {
                    case Add:
                    case Sub:
                    case Mul:
                    case Div:
                        pBuffer[position] = prim;
                        var firstChildPos = Grow(pBuffer, position + 1, maxLen, depth - 1);
                        if (firstChildPos < 0)
                            return -1; //reach max length,after adding position+2>=maxLen previous check,it should never get here
                        //return grow(pBuffer,firstChildPos,maxLen,depth - 1)
                        position = firstChildPos;
                        depth = depth - 1;
                        continue;
                    default:
                        throw new Exception();
                    //return 0; // should never get here
                }
            }
        }

        ///prog is linear formated program source code in prefix tree order
        private double fitness_function(byte[] prog)
        {
            //var len = Traverse(prog, 0);
            var fit = 0.0;
            for (var i = 0; i < _fitnesscases; i++)
            {
                for (var j = 0; j < _varNumber; j++) //fill in one input datas
                    X[j] = _targets[i][j];
                _program = prog;
                _pc = 0;
                var result = Run();
                fit += Math.Abs(result - _targets[i][_varNumber]);
            }

            return -fit;
        }

        /// return next position to traverse, equal to the len used in byte array
        private int TraverseSlow(byte[] programTree, int position, int[] endIndex)
        {
            while (true)
            {
                var cacheEnd = endIndex[position];
                if (programTree[position] < FsetStart)
                {
                    if (cacheEnd != position +1)
                        throw new Exception();
                    return ++position;
                }

                switch (programTree[position])
                {
                    case Add:
                    case Sub:
                    case Mul:
                    case Div:
                        position = TraverseSlow(programTree, ++position,endIndex);
                        //return Traverse(programTree,position)
                        continue;
                    default:
                        throw new Exception();
                    //return -1; // should never get here
                }
            }
        }

        private int TraverseAndSetNodeEndIndex(byte[] programTree, int position, int[] endIndex)
        {
            if (programTree[position] < FsetStart)
            {
                endIndex[position] = position +1;
                return ++position;
            }

            switch (programTree[position])
            {
                case Add:
                case Sub:
                case Mul:
                case Div:
                    var childEnd = TraverseAndSetNodeEndIndex(programTree, position+1, endIndex);
                    var end = TraverseAndSetNodeEndIndex(programTree, childEnd,endIndex);
                    endIndex[position] = end;
                    return end;
                default:
                    throw new Exception();
                //return -1; // should never get here
            }
        }

        private double Run()
        {
            var primitive = _program[_pc++];
            if (primitive < FsetStart)
                return X[primitive];
            switch (primitive)
            {
                case Add: return Run() + Run();
                case Sub: return Run() - Run();
                case Mul: return Run() * Run();
                case Div:
                {
                    double num = Run(), den = Run();
                    if (Math.Abs(den) <= 0.001) // guarded division
                        return num;
                    return num / den;
                }
                default:
                    throw new Exception();
            }

            //return 0.0; // should never get here
        }

        public int Evolve()
        {
            PrintParms();
            var fbestpop = Stats(_fitness, _pop, 0);
            for (var gen = 1; gen < Generations; gen++)
            {
                if (fbestpop > SymbolicRegressionBestFit)
                {
                    Console.WriteLine("PROBLEM SOLVED\n");
                    return 0;
                }

                for (var indivs = 0; indivs < PopSize; indivs++)
                {
                    byte[] newind;
                    int[] newEndIdx;
                    var ranChoose = _rd.NextDouble();
                    if (ranChoose < CrossoverProb)
                    {
                        var parent1 = Tournament(_fitness, SampleSize);
                        var parent2 = Tournament(_fitness, SampleSize);
                        newind = Crossover(_pop[parent1], _pop[parent2]
                            ,_popNodeEndIndex[parent1],_popNodeEndIndex[parent2],out newEndIdx);
                    }
                    else
                    {
                        var parent = Tournament(_fitness, SampleSize);
                        newind = PointMutation(_pop[parent], ProbPointMutPerNode
                            ,_popNodeEndIndex[parent][0]);
                        newEndIdx = _popNodeEndIndex[parent];
                    }

                    var newfit = fitness_function(newind);
                    var offspring = NegativeTournament(_fitness, SampleSize);
                    _pop[offspring] = newind;
                    _fitness[offspring] = newfit;
                    _popNodeEndIndex[offspring] = newEndIdx;
                }

                fbestpop = Stats(_fitness, _pop, gen);
            }

            Console.WriteLine("PROBLEM *NOT* SOLVED\n");
            return 1;
        }

        /// randomly choose tsize number of samples, return the index of the best fit individual amoung these samples
        private int Tournament(double[] fitness, int tsize)
        {
            var best = _rd.Next(PopSize);
            var fbest = double.MinValue;

            for (var i = 0; i < tsize; i++)
            {
                var competitor = _rd.Next(PopSize);
                if (fitness[competitor] > fbest)
                {
                    fbest = fitness[competitor];
                    best = competitor;
                }
            }

            return best;
        }

        private byte[] Crossover(byte[] parent1, byte[] parent2, int[] endIdx1, int[] endIdx2, out int[] newEndIdx)
        {
            var len1 = endIdx1[0];
            var len2 = endIdx2[0];

            var xo1Start = _rd.Next(len1); //random node selected from parent1
            var xo1End = endIdx1[xo1Start]; //the length of the selected node from start index (0)

            var xo2Start = _rd.Next(len2);
            var xo2End = endIdx2[xo2Start];

            var insertedNodeLen = xo2End - xo2Start;
            var leftoverLen = len1 - xo1End;
            var offspringLen = xo1Start + insertedNodeLen + leftoverLen;

            var offspring = new byte[offspringLen];

            System.Buffer.BlockCopy(parent1, 0, offspring, 0,
                xo1Start); //copy the first part of parent1 without the crossover node
            System.Buffer.BlockCopy(parent2, xo2Start, offspring, xo1Start,
                insertedNodeLen); //insert the selected node from parent2
            System.Buffer.BlockCopy(parent1, xo1End, offspring, xo1Start + insertedNodeLen,
                leftoverLen); //copy the second part of parent1

            newEndIdx = new int[offspringLen];
            TraverseAndSetNodeEndIndex(offspring, 0, newEndIdx);

            return offspring;
        }

        private byte[] PointMutation(byte[] parent, double probabilityPerNode, int len)
        {
            var parentcopy = new byte [len];

            System.Buffer.BlockCopy(parent, 0, parentcopy, 0, len);
            for (var i = 0; i < len; i++)
                if (_rd.NextDouble() < probabilityPerNode)
                {
                    var b = parentcopy[i];
                    if (b < FsetStart)
                        parentcopy[i] = (byte) _rd.Next(_varNumber + _randomNumber);
                    else /*if (b == Add || b == Sub || b == Mul || b == Div)*/
                    {
                        parentcopy[i] = (byte) (_rd.Next(FsetEnd - FsetStart + 1) + FsetStart);
                    }
                }

            return parentcopy;
        }

        /// randomly choose tsize number of samples, return the index of the worst fit individual amoung these samples
        private int NegativeTournament(double[] fitness, int tsize)
        {
            var worst = _rd.Next(PopSize);
            var fworst = double.MaxValue;

            for (var i = 0; i < tsize; i++)
            {
                var competitor = _rd.Next(PopSize);
                if (fitness[competitor] < fworst)
                {
                    fworst = fitness[competitor];
                    worst = competitor;
                }
            }

            return worst;
        }

        private double Stats(double[] pFitness, byte[][] pPop, int gen)
        {
            var best = _rd.Next(PopSize);
            var bestFitness = pFitness[best];
            var nodeCount = 0;
            var avgFitness = 0.0;

            for (var i = 0; i < PopSize; i++)
            {
                nodeCount += _popNodeEndIndex[i][0];
                avgFitness += pFitness[i];
                if (pFitness[i] > bestFitness)
                {
                    best = i;
                    bestFitness = pFitness[i];
                }
            }

            var avgLen = (double) nodeCount / PopSize;
            avgFitness /= PopSize;
            Console.Write(
                $"Generation={gen} Avg Fitness={-avgFitness} Best Fitness={-bestFitness} Avg Size={avgLen}\n" +
                "Best Individual: ");
            PrintIndiv(pPop[best], 0);
            Console.Write("\n");
            return bestFitness;
        }

        /// print individual, return the next index (equal to len) should visit
        private int PrintIndiv(byte[] buffer, int index)
        {
            if (buffer[index] < FsetStart)
            {
                if (buffer[index] < _varNumber)
                    Console.Write("X" + (buffer[index] + 1) + " ");//print variable name
                else
                    Console.Write(X[buffer[index]]);//print random constant
                return ++index;
            }

            var childIdx = 0;
            switch (buffer[index])
            {
                case Add:
                    Console.Write("(");
                    childIdx = PrintIndiv(buffer, ++index);
                    Console.Write(" + ");
                    break;
                case Sub:
                    Console.Write("(");
                    childIdx = PrintIndiv(buffer, ++index);
                    Console.Write(" - ");
                    break;
                case Mul:
                    Console.Write("(");
                    childIdx = PrintIndiv(buffer, ++index);
                    Console.Write(" * ");
                    break;
                case Div:
                    Console.Write("(");
                    childIdx = PrintIndiv(buffer, ++index);
                    Console.Write(" / ");
                    break;
            }

            var nextIdx = PrintIndiv(buffer, childIdx);
            Console.Write(")");
            return nextIdx;
        }

        private void PrintParms()
        {
            Console.WriteLine("-- TINY GP (C# version) --\n");
            Console.WriteLine("SEED=" + _seed + "\nMAX_LEN=" + MaxLen +
                              "\nPOPSIZE=" + PopSize + "\nDEPTH=" + Depth +
                              "\nCROSSOVER_PROB=" + CrossoverProb +
                              "\nPMUT_PER_NODE=" + ProbPointMutPerNode +
                              "\nMIN_RANDOM=" + _minRandom +
                              "\nMAX_RANDOM=" + _maxRandom +
                              "\nGENERATIONS=" + Generations +
                              "\nTSIZE=" + SampleSize +
                              "\n----------------------------------\n");
        }
    }
}