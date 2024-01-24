# Netflik: Movie Recommender based on k-Nearest Neighbor

The Movie Recommendation Engine is a system that leverages k-nearest neighbor search to provide personalized movie recommendations based on user ratings. It efficiently analyzes user preferences, identifies similar users, and suggests movies that align with the user's taste.

*Given a user and a number r of recommendations desired, this will efficiently return an ordered list of the top r movies recommended for that user from my movies dataset.*

## Motivation

This project is for the purpose of "Build to Understand", specifically exploring how k-nearest neighbor, collaborative filtering can be used in real life to build movie recommender systems like Netflix. 

## Data

Data is provided by professor Brandon Fain at Duke University. The dataset consists of roughly 1,000 users have rated a total of roughly 1,700 movies. However, most users have only rated a small subset of the movies. Rather than representing the input as a 1,000 by 1,700 matrix of ratings, we just
have a list of ratings of the form: user id, movie id, rating, timestamp.



