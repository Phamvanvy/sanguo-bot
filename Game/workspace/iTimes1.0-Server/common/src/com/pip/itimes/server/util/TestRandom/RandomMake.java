package com.pip.itimes.server.util.TestRandom;


/**
 * @author wpjiang
 * 随机数生成类
 */
public class RandomMake {
		/**
		 * 每次生成的随机数种子
		 */
		private int seed;
		/**
		 * 随机数最开始的种子
		 */
		private int orginSeed;
		public RandomMake(){
			
		}


		/**
		 * @param seed
		 * 初始话随机类最开始的种子，，6666为自己随便定义的
		 */
		public void setSeed(int seed) {
			if(seed > 6666){
				seed = seed % 6666 + 1;
			}
			this.orginSeed = seed;
			this.seed = seed;
		}
		
		/**
		 * @param bits
		 * @return 每次生成随机数
		 */
		public int  caclRandom(int bits){
			int random = 0;
			this.seed = this.seed * ( this.seed % bits) + this.seed /bits  ;
			random = (this.seed % bits);
			if(random < 0){
				random = 0 - random;
			}
			return random;
		}
		
		/**
		 * @return 随机数最原始的种子
		 */
		public int getOrginSeed(){
			return this.orginSeed;
		}
	}