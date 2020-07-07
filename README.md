Original App Design Project - README Template
===

# Recipe Storage

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
A mobile Android app developed in Java. Users can create profiles and store recipes. 

### App Evaluation
- **Category:** Food & Drink
- **Mobile:** Only available on mobile
- **Story:** Store links or descriptions of recipes you’ve liked along with pictures about how they turned out and notes about how to improve the recipe next time.
- **Market:** People who like to cook (especially using recipes from the Internet, not from physical books) and want to organize their recipes
- **Habit:** After cooking or baking something from a recipe, users record the recipe in the app.
- **Scope:** Use camera feature to take pictures, incorporate an SDK somehow

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Log in/log out of app as a user *(FBU requirement)*
* Can sign up with new user profile *(FBU requirement)*
* Can add new recipe w/ recipe ingredients, instructions, notes, prep/cook time
* Can delete recipe
* Can edit recipe
* Store at least 5 most recent recipes when user logs out using Parse *(FBU requirement: interact with a database)*
* Can take photo of what you've made and record alongside recipe *(FBU requirement: use camera)*
* [Facebook SDK](https://developers.facebook.com/docs/android/), maybe for logging in or sharing *(FBU requirement: SDK)*
* Sort OR search for recipes somehow *(FBU requirement: complex algorithm)*
* double tap to favorite *(FBU requirement: gesture recognizer)*
* animation *(FBU requirement)*
* uses an external library (eg Glide) [(more ideas)](https://medium.com/better-programming/30-best-android-libraries-and-projects-of-2019-a1e35124f110) *(FBU requirement)*

**Optional Nice-to-have Stories**

* add option to write down notes about recipe
* integrate [Spoonacular API](https://spoonacular.com/food-api) (maybe recommend recipes or pull recipes directly from Spoonacular)
* use 

### 2. Screen Archetypes

* Opening screen: Login/Sign up screen
   * requirement: log in
   * requirement: Facebook SDK

* Sign up screen
    * requirement: sign up


* Home: recipe list
   * requirement: Sort OR search for recipes at top of screen
   * requirement: add recipe button at bottom
   * requirement: delete recipe button at bottom
   * requirement: Store at least 5 most recent recipes when user logs out using Parse
   * requirement: double tap to favorite
   * requirement: animation
   * requirement: external library


* Recipe details
    * requirement: Can edit recipe


* Camera
    * requirement: Can take photo of what you've made and record alongside recipe

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* N/A (for now)

**Flow Navigation** (Screen to Screen)

* Login/Sign up Screen
=> Home: recipe list
=> Registration Screen

* Registration Screen
=> Home: recipe list

* Home: recipe list
=> recipe details

* Recipe details
=> Camera 


## Wireframes
<img src="wireframe_1.jpg" width=600>
<img src="wireframe_2.jpg" width=600>
<img src="wireframe_3.jpg" width=600>



### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
[This section will be completed in Unit 9]
### Models
[Add table of models]
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
