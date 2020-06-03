%% Load the workspace with 10 consecutively trained DQN-agents
load("Qnets_times.mat")

%% Run the simulation with 8 actions (same amount of actions as during the training)
sim2_Qnet(Qnet1)

%% Run the simulation with 6 actions
sim3_Qnet(Qnet7,6,1)