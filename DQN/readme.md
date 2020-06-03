Solving the cartpole task with a DQN agent. 
![cartpole](https://github.com/domi20u/Projects/blob/master/DQN/images/cartpole_sim.PNG)

Even though the cartpole environment has a continuous action space a DQN-agent selects among a discrete set of actions. 
[*test.m*](https://github.com/domi20u/Projects/blob/master/DQN/test.m) trains a DQN agent from scratch.
[*test_sim.m*](https://github.com/domi20u/Projects/blob/master/DQN/test_sim.m) loads the 10 pretrained models and simulates the solved cartpole environment.

The rewards from the 10 models reached the trigger condition (ten consecutive runs with a reward lower than -20) before 350 episodes total. An episode is terminated early when the cart exceeds its operational space.
![reward_full_episodes](https://github.com/domi20u/Projects/blob/master/DQN/images/cumulative_reward.png)

The exploration strategy results in the following development, where the probability of selecting a random action is shown on the y-axis.
![exploration](https://github.com/domi20u/Projects/blob/master/DQN/images/epsilon_thresh.png)

More information can be found in the [PDF](https://github.com/domi20u/Projects/blob/master/DQN/DQNvsDDPG.pdf) or [PowerPoint](https://github.com/domi20u/Projects/blob/master/DQN/DQNvsDDPG.pptx).
