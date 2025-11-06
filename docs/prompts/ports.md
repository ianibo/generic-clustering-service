
The implementation uses outdated terminology from hexagonal architectures - specifically the Port suffix.


Our local conding standards is to name interfaces for what they do not what they are.

Please refactor any interface named ...Port. Examine the interface including any javadoc comments and come up with a meaningful descriptive name then
rename the interface without the port, making sure all callers are updated. Be careful to choose good meaningful names, if necessary update
javadoc comments to explain the naming choice.
