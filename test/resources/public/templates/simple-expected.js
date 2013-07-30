angular.module("myapp").run(["$templateCache", function ($templateCache) {
  $templateCache.put("/templates/simple.html", "Howdy there! \\ Your name is \"{{ name }}\".\n");
}]);