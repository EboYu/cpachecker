function __VERIFIER_error() {}

// strict (un-) equality
if (2 === 3) { __VERIFIER_error(); }
if (3 !== 3) { __VERIFIER_error(); }
if (3 !== 3.0) { __VERIFIER_error(); }
if (3 !== 3.0) { __VERIFIER_error(); }

// binary operators
if (1 + 2 !== 3) { __VERIFIER_error(); }
if (3 + 2 === 3) { __VERIFIER_error(); }
if (5 - 2 !== 3) { __VERIFIER_error(); }
if (5 - 3 === 3) { __VERIFIER_error(); }
