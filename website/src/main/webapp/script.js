/**
 * Makes request to server
 * Sends index to an org
 * fetches names of three other orgs
 */
function findSimilarOrgs() {
  const orgIndex = document.getElementById('org-index').value;
  var indexQuery = updateQueryString('org_index', orgIndex);
  var queryString = '/predict?' + indexQuery;
  fetch(queryString).then(response => response.json()).then(text => {
    console.log(text);
    // TODO: add text to website
  })
}

/**
 * Returns an updated URL search param
 */
function updateQueryString(key, value) {
  var searchParams = new URLSearchParams();
  searchParams.append(key, value);
  return searchParams;
}
