# Manual on provided interfaces

## Intended Generator Interface


## Solution Formats
If a JSON response was requested, the solution field in the response will contain a JSON-string. 
It always contains the key "SOLUTION_FORMAT", where the value indicates how the JSON-object corresponds
to the exercise's solution. Note that the order of entries in the solution is not guaranteed.

Example JSON-String: `{"SOLUTION_FORMAT":"STRING","ANSWER0":"schwer"}`

### STRING
The other entries of the solution object are pairs mapping a (text) input element's id and name onto a solution.

Examples:

`{"SOLUTION_FORMAT":"STRING","ANSWER0":"schwer"}`

`{"ANSWER7":"8","ANSWER6":"6","SOLUTION_FORMAT":"STRING","ANSWER1":"11","ANSWER0":"3","ANSWER2":"1","ANSWER4":"7"}`

### MULTI_STRING
The other entries of the solution object are pairs mapping a (text) input element's id and name onto 
a list of possible solutions.

Examples:

`{"SOLUTION_FORMAT":"MULTI_STRING","ANSWER0":["klein", "sehr klein", "winzig"]}`

### NUMBER
The other entries of the solution object are pairs mapping a (text) input element's id and name onto
a **string** containing a number.

Examples:

`{"SOLUTION_FORMAT":"NUMBER","ANSWER0":"43.2"}`

### CHOICE
The choice solution format is used for single-choice exercises, implemented using radio buttons.
The non-SOLUTION_FORMAT entry maps the name of the radio buttons (they all share the same name) onto 
the id of the correct radio button.

Example:

`{"SOLUTION_FORMAT":"CHOICE","ANSWER":"ANSWER1"}`

### EXISTENCE
The existence solution format is used for multiple-choice exercises, implemented using checkboxes.
The non-SOLUTION_FORMAT entries map the id and name of the correct checkboxes onto the string "true".

Example:

`{"SOLUTION_FORMAT":"EXISTENCE","ANSWER0":"true"}`