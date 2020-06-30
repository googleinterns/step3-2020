/**
 * Makes request to server
 * Sends index to an org
 * fetches names of three other orgs
 */
function findSimilarOrgs() {
  const urlParams = new URLSearchParams(window.location.search);
  const index = urlParams.get('index');
  // remove previously displayed similar organizations
  const similarOrgs = document.getElementById('related-section');
  while (similarOrgs.firstChild) {
    similarOrgs.removeChild(similarOrgs.firstChild);
  }
  if (index != "-1") {
    const postQuery = '/predict?org_index=' + index;
    fetch(postQuery).then(response => response.json()).then(text => { 
      text.forEach((org) => {
        if (org.name === urlParams.get('name')) {
          similarOrgs.appendChild(getOrgAsHtmlRelated(org, org.index));
        } else {
          similarOrgs.appendChild(getOrgAsHtmlList(org));
        }
      });
    })
  } else {
    similarOrgs.appendChild(notFoundHTML(urlParams.get('name')));
  }
}

function getOrgAsHtmlRelated(org, index) {
  if (Number(org.index) === Number(index)) {
    const nameElement = document.createElement('h3');
    nameElement.innerText = 'Organizations similar to ' + org.name + ': ';
    return nameElement;
  }
  const orgElement = document.createElement('li');
  const nameElement = document.createElement('h4');
  nameElement.innerText = org.index + '. ' + org.name;
  orgElement.appendChild(nameElement);
  return orgElement;
}

/**
 * Creates list element from org
 */
function getOrgAsHtmlList(org) {
  const orgElement = document.createElement('li');
  orgElement.innerText = org.name;
  return orgElement;
}

/**
 * Returns an updated URL search param
 */
function updateQueryString(key, value) {
  var searchParams = new URLSearchParams();
  searchParams.append(key, value);
  return searchParams;
}



function notFoundHTML(query) {
  const notFound = document.createElement('li');
  notFound.innerText = "Apologies, we currently have no information on: " + query;
  return notFound;
}

/**
 * Add all existing organizations to web page
 */
function getOrgs() {
  const qs = '/sql?' + updateQueryString('keyword', '');
  fetch(qs);
}

/**
 * Add the searched organizations by keyword
 */
function searchOrgs() {
  // remove previously displayed similar organizations
  var similarOrgs = document.getElementById('existing-organizations');
  while (similarOrgs.firstChild) {
    similarOrgs.removeChild(similarOrgs.firstChild);
  }

  const keyword = document.getElementById('keyword').value;
  const qs = '/sql?' + updateQueryString('keyword', keyword);
  fetch(qs).then(response => response.json()).then(text => {
    const orgsContainer = document.getElementById('existing-organizations');
    text.forEach(entry => {
      orgsContainer.appendChild(getOrgAsHtmlDescription(entry));
    });
  })
}

/**
 * Creates list element from org
 */
function getOrgAsHtmlDescription(org) {
  const orgElement = document.createElement('li');
  const nameElement = document.createElement('h4');
  nameElement.innerText = org.index + '. ' + org.name;
  orgElement.appendChild(nameElement);
  const textElement = document.createElement('p');
  textElement.innerText = org.statement;
  orgElement.appendChild(textElement);
  return orgElement;
}
